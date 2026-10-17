# RentFlow — Multi-Tenant Landlord & Property Management Platform

10 Spring Boot microservices for property management, lease lifecycle, payments, and notifications — with **row-level tenant isolation enforced across PostgreSQL and MongoDB**, and a load-tested scaling story.

## The interesting part

**Tenant isolation** is not a side feature here — it's the design constraint driving the whole architecture.

Every HTTP request flows through a three-layer gate:

1. **Gateway** strips any client-supplied `X-Org-Id` header, verifies the JWT, and re-injects `X-Org-Id` from the token's verified `org_id` claim.
2. **Service filter** (`com.platform.common.tenant.TenantFilter`) reads the header into a ThreadLocal `TenantContext`.
3. **Repository** every query is `findByIdAndOrgId(...)` — `findById(id)` is never called from an HTTP path.

Every table and collection has `org_id` as the **leading key** in its compound indexes. Kafka consumers and scheduled jobs bypass `TenantContext` explicitly with code comments marking them internal-only.

**Result:** a landlord on org A cannot see leases, tickets, payments, or notifications belonging to org B — verified by load test.

## Load test results

5,000,000 notifications in MongoDB, seeded at ~14,400 docs/sec. Query shapes and thresholds are in [`scripts/load-test.js`](./scripts/load-test.js).

| Metric | 50 concurrent users | 1000 concurrent users |
|---|---|---|
| p95 latency | **72 ms** | 3.02 s |
| p99 latency | **132 ms** | 4.90 s |
| Throughput | 346 req/s | 414 req/s |
| HTTP failures | **0 / 270,300** | **0 / 348,088** |
| **Cross-tenant leaks** | **0 / 25,612** | **0 / 33,193** |
| Checks passed | 100% (399,126) | 100% (514,013) |

At 50 VUs the system holds well under the 200 ms target. At 1000 VUs the system saturates cleanly — it queues requests (median 2 s) but **does not error and does not leak**. Tenant isolation is scale-invariant.

Full analysis: [`LOAD_TEST.md`](./LOAD_TEST.md).

## Architecture

| Service | Stack | Port |
|---|---|---|
| `gateway-service` | Spring Cloud Gateway (reactive) | 8190 |
| `auth-service` | Spring Boot + JJWT (HS512) | 8092 |
| `lease-service` | Spring Boot + JPA | 8091 |
| `property-management-service` | Spring Boot + JPA | 8083 |
| `payment-service` | Spring Boot + JPA (11 modules) | 8186 |
| `admin-service` | Spring Boot + Feign aggregator | 8093 |
| `notification-engine` | Spring Boot + MongoDB + Kafka | 8188 |
| `maintainance-ticket-service` | Spring Boot + JPA + Kafka producer | 8084 |
| `mpesa-service` | Spring Boot + Safaricom Daraja | 8189 |
| `eureka-server` | Spring Cloud Netflix Eureka | 8761 |
| `common` | Shared DTOs, `TenantContext`, Kafka event schemas | — |

**Databases:** PostgreSQL 15 (relational) + MongoDB 7 (notifications).
**Events:** Kafka with per-service consumers, JSON payloads carrying `organizationId`.

## Tech stack

- Java 17, Spring Boot 3.3
- Spring Cloud Gateway, Eureka, Spring Cloud LoadBalancer
- Spring Data JPA (Hibernate 6), Spring Data MongoDB
- Flyway (PostgreSQL schema), `@CompoundIndex` (Mongo)
- Spring Security (JWT resource server patterns at the gateway)
- JJWT (HS512 tokens with `org_id` claim)
- Kafka (Spring Kafka + JSON payloads)
- Prometheus, Micrometer, Zipkin (distributed tracing)
- Docker Compose (local), Nginx (prod reverse proxy)
- k6 (load testing), Python + pymongo (data seeding)

## Running locally

**Prerequisites:** JDK 17, Docker Desktop, Maven wrapper (bundled).

```bash
# 1. Start infrastructure
docker compose up -d postgres mongodb redis kafka zookeeper

# 2. Start services (each in its own terminal, or use the RUNBOOK)
./mvnw -pl common install -DskipTests
./mvnw -pl eureka-server spring-boot:run
./mvnw -pl gateway-service spring-boot:run -Dspring-boot.run.profiles=local
./mvnw -pl auth-service spring-boot:run -Dspring-boot.run.profiles=local
# ... etc for each service