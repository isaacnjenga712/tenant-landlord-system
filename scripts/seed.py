#!/usr/bin/env python3
"""
Seed 5M notifications:
  50 orgs x 100 recipients/org x 1000 notifications/recipient = 5,000,000

Deterministic UUIDs so the load test can target exact (org, recipient) pairs:
  org       = 00000000-0000-0000-0000-{tenant:012d}       (tenant 1..50)
  recipient = 00000000-0000-0000-{tenant:04d}-{unit:012d} (unit 1..100)

The k6 script (rentflow-loadtest.js) uses the SAME scheme. Do not change
without updating the load test.

Usage:
  python seed.py                # interactive seed (drops if non-empty)
  python seed.py --force        # drop and reseed without prompting
  python seed.py --verify       # run explain() and print index stats
  python seed.py --drop         # just drop the collection, no seeding
  python seed.py --no-index     # skip index creation (dev only)

Environment:
  MONGO_URI  default mongodb://localhost:27018/
  MONGO_DB   default notification_db
"""
import argparse
import os
import sys
import time
from datetime import datetime, timedelta, timezone
from uuid import UUID

from pymongo import MongoClient, ASCENDING, DESCENDING
from pymongo.errors import PyMongoError

MONGO_URI   = os.environ.get("MONGO_URI", "mongodb://localhost:27018/")
DB_NAME     = os.environ.get("MONGO_DB",  "notification_db")
COLL_NAME   = "notifications"

TENANTS          = 50
UNITS_PER_TENANT = 100
NOTIFS_PER_UNIT  = 1000
BATCH            = 10_000

EVENT_TYPES = ["LEASE_CREATED", "INVOICE_CREATED", "PAYMENT_RECEIVED", "MAINTENANCE_CREATED"]

INDEX_NAME = "org_recipient_created_idx"
INDEX_KEYS = [("organizationId", ASCENDING),
              ("recipientPublicId", ASCENDING),
              ("createdAt", DESCENDING)]


# ---------------------------------------------------------------------------
# Deterministic UUID helpers — MUST match rentflow-loadtest.js
# ---------------------------------------------------------------------------
def uuid_org(tenant: int) -> UUID:
    return UUID(f"00000000-0000-0000-0000-{tenant:012d}")


def uuid_recipient(tenant: int, unit: int) -> UUID:
    return UUID(f"00000000-0000-0000-{tenant:04d}-{unit:012d}")


# ---------------------------------------------------------------------------
# Index management
# ---------------------------------------------------------------------------
def ensure_indexes(coll):
    """Create the compound index if missing. Idempotent."""
    existing = {idx["name"]: idx for idx in coll.list_indexes()}

    if INDEX_NAME in existing:
        # SON / dict comparison — do NOT unpack tuples here
        actual_keys = existing[INDEX_NAME]["key"]
        expected_keys = dict(INDEX_KEYS)
        if dict(actual_keys) == expected_keys:
            print(f"Index '{INDEX_NAME}' already present — OK.")
            return
        print(f"Index '{INDEX_NAME}' present but keys differ:")
        print(f"  existing: {dict(actual_keys)}")
        print(f"  expected: {expected_keys}")
        print("Dropping and recreating.")
        coll.drop_index(INDEX_NAME)

    print(f"Building index '{INDEX_NAME}' on {INDEX_KEYS} ...")
    t0 = time.time()
    coll.create_index(INDEX_KEYS, name=INDEX_NAME)
    print(f"  built in {time.time() - t0:.1f}s")

def print_indexes(coll):
    print("\nIndexes:")
    for idx in coll.list_indexes():
        print(f"  {idx['name']}: {idx['key']}")


# ---------------------------------------------------------------------------
# Verification — prove the index is actually used at 5M rows
# ---------------------------------------------------------------------------
def verify(coll):
    """Run an explain() on the inbox read path and print key stats."""
    org_id       = uuid_org(1)
    recipient_id = uuid_recipient(1, 1)

    query = {"organizationId": org_id, "recipientPublicId": recipient_id}
    sort  = [("createdAt", DESCENDING)]

    print(f"\nVerifying index usage for query:")
    print(f"  organizationId      = {org_id}")
    print(f"  recipientPublicId   = {recipient_id}")
    print(f"  sort                = {sort}")

    try:
        explain = coll.find(query).sort(sort).limit(20).explain()
    except PyMongoError as e:
        print(f"explain() failed: {e}")
        return False

    # executionStats is at the top level per your diagnostic output.
    stats = explain.get("executionStats") or {}

    n_returned    = stats.get("nReturned", "?")
    exec_ms       = stats.get("executionTimeMillis", "?")
    docs_examined = stats.get("totalDocsExamined", "?")
    keys_examined = stats.get("totalKeysExamined", "?")

    winning_plan = explain.get("queryPlanner", {}).get("winningPlan", {})
    index_name   = _find_index_name(winning_plan)
    used_collscan = _uses_collscan(winning_plan)

    print(f"\n  nReturned         : {n_returned}")
    print(f"  executionTimeMs   : {exec_ms}")
    print(f"  totalDocsExamined : {docs_examined}   <- should be ~ nReturned, not 5,000,000")
    print(f"  totalKeysExamined : {keys_examined}   <- should be ~ nReturned, not 5,000,000")
    print(f"  winning index     : {index_name or 'NONE (COLLSCAN)'}")
    print(f"  uses COLLSCAN     : {used_collscan}")

    ok = (
        index_name == INDEX_NAME
        and not used_collscan
        and isinstance(docs_examined, int)
        and docs_examined <= 100
    )
    print(f"\n  index verification: {'PASS' if ok else 'FAIL'}")
    return ok


def _find_index_name(plan):
    """Recursively find the indexName in a Mongo winning plan.
    Handles classic plans, SBE plans, and SON-shaped dicts."""
    if isinstance(plan, dict):
        if "indexName" in plan:
            return plan["indexName"]
        for v in plan.values():
            r = _find_index_name(v)
            if r:
                return r
    elif isinstance(plan, list):
        for v in plan:
            r = _find_index_name(v)
            if r:
                return r
    elif isinstance(plan, str):
        # SBE slotBasedPlan string form, e.g. "...ixscan [org_recipient_created_idx]..."
        import re
        m = re.search(r"ixscan\s+\[([^\]]+)\]", plan)
        if m:
            return m.group(1)
    return None


def _uses_collscan(plan):
    """Return True if the winning plan uses COLLSCAN."""
    if isinstance(plan, dict):
        if plan.get("stage") == "COLLSCAN":
            return True
        for v in plan.values():
            if _uses_collscan(v):
                return True
    elif isinstance(plan, list):
        for v in plan:
            if _uses_collscan(v):
                return True
    elif isinstance(plan, str):
        return "COLLSCAN" in plan
    return False

# ---------------------------------------------------------------------------
# Seeding
# ---------------------------------------------------------------------------
def seed(coll, force: bool, create_index: bool):
    existing = coll.count_documents({})
    if existing > 0:
        print(f"Collection already has {existing:,} docs.")
        if force:
            print("--force set, dropping.")
        else:
            resp = input("Drop and reseed? [y/N] ").strip().lower()
            if resp != "y":
                print("Aborting.")
                return False
        coll.drop()
        print("Dropped.")

    total = TENANTS * UNITS_PER_TENANT * NOTIFS_PER_UNIT
    print(f"Seeding {total:,} notifications...")

    now     = datetime.now(timezone.utc)
    inserted = 0
    batch    = []
    t0       = time.time()

    try:
        for t in range(1, TENANTS + 1):
            org_id = uuid_org(t)
            for u in range(1, UNITS_PER_TENANT + 1):
                recipient_id = uuid_recipient(t, u)
                for i in range(NOTIFS_PER_UNIT):
                    batch.append({
                        "organizationId":    org_id,
                        "recipientPublicId": recipient_id,
                        "eventType":         EVENT_TYPES[i % len(EVENT_TYPES)],
                        "subject":           f"Notification {i}",
                        "body":              f"Body for notif {i}",
                        "read":              (i % 3 == 0),
                        "createdAt":         now - timedelta(minutes=i),
                    })
                    if len(batch) >= BATCH:
                        coll.insert_many(batch, ordered=False)
                        inserted += len(batch)
                        batch.clear()
                        elapsed = time.time() - t0
                        rate = inserted / elapsed if elapsed else 0
                        print(f"  inserted={inserted:>10,}  "
                              f"rate={rate:>8,.0f}/s  "
                              f"elapsed={elapsed:>6.1f}s")
    except KeyboardInterrupt:
        print("\nInterrupted. Flushing batch before exit.")
    except PyMongoError as e:
        print(f"\nMongo error during insert: {e}")
        print(f"Aborted at {inserted:,} docs.")
        return False

    if batch:
        coll.insert_many(batch, ordered=False)
        inserted += len(batch)

    elapsed = time.time() - t0
    final   = coll.count_documents({})
    print(f"\nDone. {final:,} docs in {elapsed:.1f}s "
          f"({final / elapsed:,.0f}/s)")

    if create_index:
        ensure_indexes(coll)

    print_indexes(coll)
    return final == total


# ---------------------------------------------------------------------------
# CLI
# ---------------------------------------------------------------------------
def main():
    p = argparse.ArgumentParser(description="Seed 5M multi-tenant notifications.")
    p.add_argument("--force",    action="store_true", help="drop without prompting")
    p.add_argument("--drop",     action="store_true", help="drop only, no seeding")
    p.add_argument("--verify",   action="store_true", help="run explain() and print stats")
    p.add_argument("--no-index", action="store_true", help="skip index creation")
    args = p.parse_args()

    client = MongoClient(
        MONGO_URI,
        serverSelectionTimeoutMS=5000,
        uuidRepresentation="standard",
    )
    db   = client[DB_NAME]
    coll = db[COLL_NAME]

    # Fail fast if Mongo isn't reachable
    try:
        client.server_info()
    except PyMongoError as e:
        print(f"Could not connect to {MONGO_URI}: {e}")
        sys.exit(1)

    print(f"Connected to {MONGO_URI} -> {DB_NAME}.{COLL_NAME}")

    if args.drop:
        coll.drop()
        print("Dropped.")
        return

    if args.verify:
        ok = verify(coll)
        sys.exit(0 if ok else 1)

    ok = seed(coll, force=args.force, create_index=not args.no_index)

    if ok:
        verify(coll)   # always verify at the end — proves the index is used
        sys.exit(0)
    else:
        print("\nSeeding did not complete as expected.")
        sys.exit(1)


if __name__ == "__main__":
    main()