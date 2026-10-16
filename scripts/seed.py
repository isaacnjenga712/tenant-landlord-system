#!/usr/bin/env python3
"""
Seed 5M notifications:
  50 orgs x 100 recipients/org x 1000 notifications/recipient = 5,000,000

Deterministic UUIDs so the load test can target exact (org, recipient) pairs:
  org       = 00000000-0000-0000-0000-{tenant:012d}       (tenant 1..50)
  recipient = 00000000-0000-0000-{tenant:04d}-{unit:012d} (unit 1..100)
"""
import os
import time
from uuid import UUID
from datetime import datetime, timedelta, timezone

from pymongo import MongoClient

MONGO_URI = os.environ.get("MONGO_URI", "mongodb://localhost:27018/")
DB_NAME = os.environ.get("MONGO_DB", "notification_db")
COLL_NAME = "notifications"

TENANTS = 50
UNITS_PER_TENANT = 100
NOTIFS_PER_UNIT = 1000
BATCH = 10_000

EVENT_TYPES = ["LEASE_CREATED", "INVOICE_CREATED", "PAYMENT_RECEIVED", "MAINTENANCE_CREATED"]


def uuid_org(tenant: int) -> UUID:
    return UUID(f"00000000-0000-0000-0000-{tenant:012d}")


def uuid_recipient(tenant: int, unit: int) -> UUID:
    return UUID(f"00000000-0000-0000-{tenant:04d}-{unit:012d}")


def main():
    client = MongoClient(
        MONGO_URI,
        serverSelectionTimeoutMS=5000,
        uuidRepresentation="standard",
    )
    db = client[DB_NAME]
    coll = db[COLL_NAME]

    # Fail fast if Mongo isn't reachable
    client.server_info()
    print(f"Connected to {MONGO_URI} -> {DB_NAME}.{COLL_NAME}")

    existing = coll.count_documents({})
    if existing > 0:
        print(f"Collection already has {existing:,} docs.")
        resp = input("Drop and reseed? [y/N] ").strip().lower()
        if resp != "y":
            print("Aborting.")
            return
        coll.drop()
        print("Dropped.")

    total = TENANTS * UNITS_PER_TENANT * NOTIFS_PER_UNIT
    print(f"Seeding {total:,} notifications...")

    now = datetime.now(timezone.utc)
    inserted = 0
    batch = []
    t0 = time.time()

    for t in range(1, TENANTS + 1):
        org_id = uuid_org(t)
        for u in range(1, UNITS_PER_TENANT + 1):
            recipient_id = uuid_recipient(t, u)
            for i in range(NOTIFS_PER_UNIT):
                batch.append({
                    "organizationId": org_id,
                    "recipientPublicId": recipient_id,
                    "eventType": EVENT_TYPES[i % len(EVENT_TYPES)],
                    "subject": f"Notification {i}",
                    "body": f"Body for notif {i}",
                    "read": (i % 3 == 0),
                    "createdAt": now - timedelta(minutes=i),
                })
                if len(batch) >= BATCH:
                    coll.insert_many(batch, ordered=False)
                    inserted += len(batch)
                    batch.clear()
                    elapsed = time.time() - t0
                    rate = inserted / elapsed if elapsed else 0
                    print(f"  inserted={inserted:>10,}  rate={rate:>8,.0f}/s  elapsed={elapsed:>6.1f}s")

    if batch:
        coll.insert_many(batch, ordered=False)
        inserted += len(batch)

    elapsed = time.time() - t0
    final = coll.count_documents({})
    print(f"\nDone. {final:,} docs in {elapsed:.1f}s.")

    print("\nIndexes:")
    for idx in coll.list_indexes():
        print(f"  {idx['name']}: {idx['key']}")


if __name__ == "__main__":
    main()