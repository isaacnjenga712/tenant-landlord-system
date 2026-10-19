# Load Test - Multi-Tenant Isolation

Real load test results demonstrating that tenant isolation holds under sustained concurrent load, with a documented performance tuning phase.

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

## Results: The Tuning Story

To find the system's capacity, the 1,000 VU test was run twice: once with Spring Boot defaults, and once after tuning the connection pools.

### Run 1 - 1,000 VUs (Default Config)
*Limited by Tomcat threads (200) and Mongo connection pool.*

| Metric | Value | Target | Status |
|---|---:|---|---|
| **p95 latency** | **3.02 s** | `< 200 ms` | ❌ Threshold crossed |
| **p99 latency** | **4.90 s** | `< 500 ms` | ❌ Threshold crossed |
| Throughput | 414 req/s | - | - |
| **HTTP failures** | **0 / 348,088** | `< 0.1%` | ✅ |
| **Cross-tenant leaks** | **0 / 33,193** | **0 required** | ✅ |

### Run 2 - 1,000 VUs (Tuned Config)
*Increased Tomcat threads to 400, Mongo maxPoolSize to 200, and JVM heap to 2GB.*

| Metric | Value | Target | Status |
|---|---:|---|---|
| **p95 latency** | **1.75 s** | `< 200 ms` | ❌ CPU Limit reached |
| **p99 latency** | **1.91 s** | `< 500 ms` | ❌ CPU Limit reached |
| Throughput | **710 req/s** | - | **+71% Capacity** |
| **HTTP failures** | **0 / 128,819** | `< 0.1%` | ✅ |
| **Cross-tenant leaks** | **0 / 12,186** | **0 required** | ✅ |

**The Takeaway:**
Tuning the connection pools increased throughput by **71%** (414 -> 710 req/s) and reduced p95 latency by **42%** (3.02s -> 1.75s). While the single-laptop CPU prevents holding the <200ms target at 1,000 VUs, the system degrades gracefully. Most importantly, **tenant isolation held perfectly at every scale and configuration.**

## Index verification at 5M rows

`explain("executionStats")` on the inbox query — the most common read path:

```text
nReturned: 20
executionTimeMs: < 10
totalDocsExamined: 20  <- not 5,000,000
totalKeysExamined: 20  <- not 5,000,000
innerIndexName: org_recipient_created_idx

Architecture that made this possible
Gateway: Strips any client-supplied X-Org-Id before verifying the JWT. Then it re-injects X-Org-Id from the token's org_id claim.

Service filter: com.platform.common.tenant.TenantFilter reads the header into a thread-local TenantContext.

Repository: Every HTTP-path read uses findByOrgIdAnd...; findAll is never called from an HTTP path.

Async paths: Kafka consumers and scheduled jobs bypass TenantContext explicitly, with comments marking them internal-only.

Storage: Every MongoDB collection has an organizationId-first compound index. Every PostgreSQL table has an org_id column with composite indexes leading with org_id.

Known limitations
Single-machine test. The k6 client and Spring Boot server run on the same laptop. Real deployments would have different latency characteristics.

CPU Bound. At 1,000 VUs, the single laptop CPU saturates, causing queueing (p95 = 1.75s). Tuning pushed the knee, but hardware is the final limit.

Results are environment-specific. Hardware, OS, connection-pool settings, and client placement can all affect throughput and latency.

Stack
Java 17, Spring Boot 3.3

Spring Data MongoDB, MongoDB 7.0

Spring Cloud Gateway, Spring Security (JWT)

Kafka (Spring Kafka)

Flyway (PostgreSQL), @CompoundIndex (MongoDB)

k6 for load generation

Python 3.12 + pymongo for seeding
