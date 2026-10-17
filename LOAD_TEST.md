# Load Test — Multi-Tenant Isolation



Real load test results demonstrating that tenant isolation holds under sustained concurrent load.



## What was tested



**5,000,000 notifications** in MongoDB, each scoped by `org_id`, served by a Spring Boot 3.3 microservice. The goal: prove that **a user on org A cannot read data belonging to org B**, no matter how much concurrent load the system is under.



## Data shape



| Dimension | Value |

|---|---|

| Tenants (orgs) | 50 |

| Recipients per tenant | 100 |

| Notifications per recipient | 1,000 |

| **Total documents** | **5,000,000** |

| Index | `(organizationId, recipientPublicId, createdAt DESC)` |



**Deterministic UUIDs** ensure that the k6 test targets exact `(org, recipient)` pairs. Data was seeded via `scripts/seed.py` (Python + pymongo) in **346.6 seconds at approximately 14,400 documents/sec**.



## Query shapes and mix



| Scenario | Mix | Index used |

|---|---:|---|

| Inbox list (paginated, 20 items) | 60% | `org_recipient_created_idx` |

| Unread count | 25% | `org_recipient_unread_idx` |

| Cross-tenant negative (same recipient, wrong org) | 10% | Same index, different org value |

| Mark-read (write path) | 5% | — |



Full scenarios and thresholds: [`scripts/load-test.js`](./scripts/load-test.js).



## Results



### Run 1 — 50 concurrent users, 14 minutes



| Metric | Value | Target | Status |

|---|---:|---|---|

| **p95 latency** | **72 ms** | `< 200 ms` | ✅ |

| **p99 latency** | **132 ms** | `< 500 ms` | ✅ |

| Throughput | 346 req/s | — | — |

| Total requests | 270,300 | — | — |

| **HTTP failures** | **0 / 270,300** | `< 0.1%` | ✅ |

| **Cross-tenant leaks** | **0 / 25,612** | **0 required** | ✅ |

| Checks passed | 100% (399,126 / 399,126) | 100% | ✅ |

| Data transferred | 810 MB | — | — |



### Run 2 — 1,000 concurrent users, 14 minutes



| Metric | Value | Target | Status |

|---|---:|---|---|

| **p95 latency** | **3.02 s** | `< 200 ms` | ❌ Threshold crossed |

| **p99 latency** | **4.90 s** | `< 500 ms` | ❌ Threshold crossed |

| Throughput | 414 req/s | — | — |

| Total requests | 348,088 | — | — |

| **HTTP failures** | **0 / 348,088** | `< 0.1%` | ✅ |

| **Cross-tenant leaks** | **0 / 33,193** | **0 required** | ✅ |

| Checks passed | 100% (514,013 / 514,013) | 100% | ✅ |

| Data transferred | 1.0 GB | — | — |



**The p95/p99 threshold failure at 1,000 VUs is the expected result.** We ran until we found the knee in the curve, not until the system broke. That's the point of load testing to saturation.



## What the scaling curve shows



| Metric | 50 VUs | 1,000 VUs | Change |

|---|---:|---:|---:|

| p95 | 72 ms | 3.02 s | **42×** |

| Median | 21 ms | 2.0 s | **95×** |

| Throughput | 346 req/s | 414 req/s | **1.2×** |

| Leaks | 0 | 0 | — |

| Errors | 0 | 0 | — |



**20× the concurrency produced approximately 20% more throughput and 42× the p95 latency, with zero errors and zero cross-tenant leaks.**



At 1,000 concurrent users, the system queues requests — median response 2 seconds. It does not error, and it does not leak. It degrades in latency, not in correctness.



## Index verification at 5M rows



`explain("executionStats")` on the inbox query — the most common read path:



```text

nReturned: 20

executionTimeMs: < 10

totalDocsExamined: 20 ← not 5,000,000

totalKeysExamined: 20 ← not 5,000,000

stage: LIMIT

inputStage: FETCH

innerStage: IXSCAN

innerIndexName: org_recipient_created_idx

```



The composite index does its job: a 20-document page examines 20 index keys and 20 documents, rather than scanning 5 million documents.



Without this index, query latency could increase substantially, potentially reaching seconds under load. Actual performance depends on the query plan, data distribution, and hardware.



## Architecture that made this possible



1\. **Gateway:** Strips any client-supplied `X-Org-Id` before verifying the JWT. Then it re-injects `X-Org-Id` from the token's `org_id` claim.

2\. **Service filter:** `com.platform.common.tenant.TenantFilter` reads the header into a thread-local `TenantContext`.

3\. **Repository:** Every HTTP-path read uses `findByOrgIdAnd...`; `findAll` is never called from an HTTP path.

4\. **Async paths:** Kafka consumers and scheduled jobs bypass `TenantContext` explicitly, with comments marking them internal-only.

5\. **Storage:** Every MongoDB collection has an `organizationId`-first compound index. Every PostgreSQL table has an `org_id` column with composite indexes leading with `org_id`.



## What did NOT happen under load



- **Zero cross-tenant leakage.** 33,193 deliberate attempts to read another organization's data at 1,000 VUs — none succeeded.

- **Zero HTTP failures.** 348,088 requests, no 500s, no timeouts, no connection-pool exhaustion.

- **Zero assertion failures.** 100% of assertions passed across 514,013 checks, including 33,193 deliberate cross-tenant access attempts.



## Reproducing



### 1. Seed 5M documents



Requirements: Python 3.10+ and MongoDB available at the connection settings configured in `scripts/seed.py`.



```bash

# Install the MongoDB Python driver

pip install pymongo



# Seed the database

python scripts/seed.py

```



Expected output:



```text

Done. 5,000,000 docs in ~350s.

```



### 2. Start notification-engine



From the project root:



```bash

./mvnw -pl notification-engine spring-boot:run \\

&#x20; -Dspring-boot.run.profiles=local

```



Wait for Tomcat to start on port `8188`.



### 3. Run the load test



Install k6 using the [official installation guide](https://k6.io/docs/get-started/installation/).



Run the test:



```bash

k6 run scripts/load-test.js

```



The script uses `ramping-vus`: 2 minutes ramp-up, 10 minutes steady state, and 2 minutes ramp-down. The script is configured for 1,000 VUs by default. Override with `--vus` and `--duration` for shorter smoke tests.



## Known limitations



- **Single-machine test.** The k6 client and Spring Boot server run on the same laptop. Real deployments would have different latency characteristics.

- **No network latency between services.** All services run on localhost.

- **MongoDB at the default connection-pool configuration.** Increasing the pool may move the latency knee, but this needs to be measured rather than assumed.

- **No sustained soak test.** The 1,000-VU run lasted 14 minutes. A 24-hour soak test could reveal memory leaks or gradual degradation that this test does not cover.

- **Results are environment-specific.** Hardware, operating system, connection-pool settings, database configuration, and client placement can all affect throughput and latency.



## Stack



- Java 17, Spring Boot 3.3

- Spring Data MongoDB, MongoDB 7.0

- Spring Cloud Gateway, Spring Security (JWT)

- Kafka (Spring Kafka)

- Flyway (PostgreSQL), `@CompoundIndex` (MongoDB)

- k6 for load generation

- Python 3.12 + pymongo for seeding



