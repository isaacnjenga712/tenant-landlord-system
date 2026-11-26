# RentFlow - Multi-Tenant Property Management System

**Status:** Pilot complete - **GO** decision.

## Tenant Isolation

| Probe | Count | Leaks |
|---|---:|---:|
| Cross-tenant GET | 6,740 | **0** |
| Cross-tenant WRITE | 3,513 | **0** |
| **Total** | **10,253** | **0** |

**Negative control:** 100/100 leak detections.

## Audit Log Counts (live)

| Event type | Count |
|---|---:|
| USER_LOGIN | 24 |
| USER_REGISTERED | 2 |


## Pilot Results

5-day pilot: **GO**.

## Service Status

| Service | Status |
|---|---|
| rentflow-admin | Up 5 days (healthy) |
| rentflow-auth | Up 5 days (healthy) |
| rentflow-eureka | Up 5 days (healthy) |
| rentflow-gateway | Up 5 days (healthy) |
| rentflow-grafana | Up 5 days |
| rentflow-kafka | Up 5 days |
| rentflow-kafka-exporter | Up 5 days |
| rentflow-lease | Up 5 days (healthy) |
| rentflow-maintenance | Up 5 days (healthy) |
| rentflow-mongodb | Up 5 days (healthy) |
| rentflow-mpesa | Up 5 days (healthy) |
| rentflow-node-exporter | Up 5 days |
| rentflow-notification | Up 5 days (healthy) |
| rentflow-payment | Up 5 days (healthy) |
| rentflow-postgres | Up 5 days (healthy) |
| rentflow-prometheus | Up 5 days |
| rentflow-redis | Up 5 days (healthy) |
| rentflow-zipkin | Up 13 hours (healthy) |


## Recent Commits

- ef13299 docs: complete 5-day pilot (GO decision)
- bc35c26 test: add Day 2 active usage script (5 iterations, 2 users)
- 399049e docs: add pilot plan and cross-tenant probe scripts (Day 1)
- eef4f6f fix: add @EntityScan and @EnableJpaRepositories for common audit entities
- 14c2ffa ´╗┐feat: add security audit log with per-tenant retention (Layer 2)
- 734696b ´╗┐fix(property): idempotent Flyway migrations + schema sync
- d207a48 ´╗┐chore: reduce log volume by 80-90% (Layer 1 data reduction)
- bd25bf8 Merge pull request #2 from isaacnjenga712/feat/microservices-gateway
- eaebf52 Merge pull request #2 from isaacnjenga712/feat/microservices-gateway
- b821af7 chore: gitignore load-test summary.json