# Pilot Report — RentFlow Multi-Tenant Isolation

## Decision: GO

All success measures met. The pilot is ready for a wider rollout.

## Results

| Measure | Target | Actual | Verdict |
|---|---|---|---|
| Cross-tenant leaks | 0 | 0 | ✅ |
| Audit log coverage | 100% | 100% | ✅ |
| Log volume | < 50 lines/session | 30/27/15 | ✅ |
| Workflow completion | 5/5 | 5/5 | ✅ |
| Load test regression | 0 leaks | 0 | ✅ |

## Evidence

- **PILOT-EVIDENCE.md** — cross-tenant probe results (GET: total 0, WRITE: 404)
- **k6 smoke test** — 244 GET probes, 129 WRITE probes, 0 leaks, 0 inconclusive
- **Audit log** — 5 USER_LOGIN rows from the 5-iteration script
- **Log volume** — auth 30, notification 27, gateway 15 lines (all under 50)

## Day 1 (Prepare)

- All 17 containers `Up (healthy)`
- Pilot users seeded: org1test (ADMIN), pilot2 (TENANT)
- Audit log verified: USER_LOGIN and USER_REGISTERED captured
- Full workflow tested: login -> inbox (200 items) -> mark-read

## Day 2 (Active Usage)

- 5/5 iterations succeeded (alternating org1test and pilot2)
- Audit log: 5 USER_LOGIN rows
- Log volume: 30/27/15 lines (under 50 target)
- No errors in auth or notification logs

## Day 3 (Cross-Tenant Probes)

- Probe 1 (GET): 200 OK, total 0 — scoped correctly
- Probe 2 (WRITE): 404 Not Found — notification not in org 2
- k6 smoke test: 244 GET probes, 129 WRITE probes, 0 leaks, 0 inconclusive

## What's Tested

- Service-level isolation (read + write) at pilot scale
- Audit log coverage for login/register
- Log volume under real usage
- Full workflow repeatability

## What's Not Tested

- Gateway isolation under load (covered by unit tests)
- Async paths under load (Kafka, scheduled jobs)
- Multi-org user mixing (pilot used org 1 only)

## Next Steps

1. Onboard 5-10 users from a second org
2. Run the pilot for 2 weeks
3. Monitor audit log growth and log volume trends
4. Proceed to a wider rollout if successful