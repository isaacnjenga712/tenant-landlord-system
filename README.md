# RentFlow - Multi-Tenant Landlord & Property Management Platform

10 Spring Boot microservices for property management, lease lifecycle, payments, and notifications, with row-level tenant isolation enforced across PostgreSQL and MongoDB.

## Load test results

5,000,000 notifications seeded in MongoDB, tested with k6:

- **50 concurrent users:** p95 = 72ms, p99 = 132ms, 0 leaks / 25,612 negative checks
- **1000 concurrent users:** p95 = 3.02s, 0 HTTP failures / 348,088 requests, **0 leaks / 33,193 negative checks**

The isolation invariant held at both scales. The system degrades in latency under saturation, not in correctness.

## Services

Gateway, auth, lease, property-management, payment, admin, notification-engine, maintenance-ticket, mpesa, eureka. Plus a shared `common` module for `TenantContext`, DTOs, and Kafka events.

## Stack

Java 17, Spring Boot 3.3, Spring Cloud Gateway, PostgreSQL 15, MongoDB 7, Kafka, Flyway, JWT (JJWT), Docker Compose, k6 for load testing.

## Documentation

- ARCHITECTURE.md - service design and data flows
- API.md - REST API reference
- RUNBOOK.md - local setup and troubleshooting
- DEPLOY.md - production deployment
- LOAD_TEST.md - full load test methodology and results

## Why this exists

Portfolio project to explore what it takes to enforce multi-tenant isolation at scale. The interesting result wasn't the latency - it was that the isolation invariant held even when the system saturated. Latency degrades; correctness doesn't.
