# RentFlow — Operations Runbook

Operational guide for developing, debugging, and recovering the RentFlow stack.

Companion to `ARCHITECTURE.md` and `API.md`. Commands are labeled by execution environment and intended for local development.


## Prerequisites

- Docker Desktop with Docker Compose
- Git Bash or WSL on Windows
- Node.js and npm
- Java/JDK and Maven Wrapper (`./mvnw`)
- Python 3
- `curl`, `grep`, `find`, and standard shell utilities
- `jq` (recommended for JSON inspection)
- `ngrok` for M-Pesa callback testing
- Tested environment: Windows 11 + Git Bash / CMD
- Last updated: 2026-10-03
- RentFlow version/commit: **record the commit used for this environment here**

## Shell and Docker conventions

Use Git Bash for Unix-style commands and CMD only where explicitly labeled. In Git Bash, `MSYS_NO_PATHCONV=1` prevents MSYS from rewriting Docker paths before they reach the container.

`docker compose ...` uses **Compose service names**. Names such as `rentflow-auth` are **container names**. They are not interchangeable. Verify the mapping with `docker compose ps`.

At the repository root:

```bash
# Git Bash (host)
REPO_ROOT="${REPO_ROOT:-$HOME/Downloads/tenant-landlord-system/tenant-landlord-system}"
cd "$REPO_ROOT"
```

### Container-name mapping

| Compose service | Container name | Host port |
|---|---|---:|
| `eureka-service` | `rentflow-eureka` | 8761 |
| `gateway-service` | `rentflow-gateway` | 8080 |
| `auth-service` | `rentflow-auth` | 8090 |
| `property-management-service` | `rentflow-property` | 8082 |
| `lease-service` | `rentflow-lease` | 8085 |
| `payment-service` | `rentflow-payment` | 8086 |
| `mpesa-service` | `rentflow-mpesa` | 8089 |
| `maintainance-ticket-service` | `rentflow-maintenance` | 8084 |
| `notification-engine` | `rentflow-notification` | 8081 |

Use `docker compose restart <service>` for a Compose service, or `docker restart <container>` for a running container.


RentFlow — Operations Runbook

Operational guide for developing, debugging, and recovering the RentFlow stack.

Companion to ARCHITECTURE.md and API.md. Every command here has been exercised during development.

Table of Contents

Quick Reference

Health Checks

Common Symptoms → Fixes

Service-Specific Runbooks

Database Operations

Kafka Operations

Frontend Operations

Port & Process Management

Common JAR Distribution

M-Pesa & Safaricom Operations

Emergency Recovery

Log Collection for Debugging

## 1. Quick Reference

### 1.1 URLs

| What | URL |
| --- | --- |
| Frontend (nginx prod) | http://localhost |
| Frontend (Vite dev) | http://localhost:5173 |
| API Gateway | http://localhost:8080 |
| Eureka dashboard | http://localhost:8761 |
| Grafana | http://localhost:3000 (admin / admin) |
| Prometheus | http://localhost:9090 |
| Zipkin | http://localhost:9411 |
| ngrok inspector | http://127.0.0.1:4040 |


### 1.2 Test credentials



| Role | Email | Password |
| --- | --- | --- |
| Tenant | bob@test.com | Passw0rd! |
| Landlord | alice@test.com | Passw0rd! |
| Legacy tenant | tenant@test.com | Passw0rd! |
| Legacy landlord | landlord@test.com | Passw0rd! |


### 1.3 Container names & ports

| Container | Host Port | Container Port |
| --- | --- | --- |
| rentflow-eureka | 8761 | 8761 |
| rentflow-gateway | 8080 | 8080 |
| rentflow-auth | 8090 | 8080 |
| rentflow-property | 8082 | 8082 |
| rentflow-lease | 8085 | 8081 |
| rentflow-payment | 8086 | 8086 |
| rentflow-mpesa | 8089 | 8088 |
| rentflow-maintenance | 8084 | 8084 |
| rentflow-notification | 8081 | 8080 |
| rentflow-nginx | 80 | 80 |
| rentflow-postgres | 5432 | 5432 |
| rentflow-mongodb | — | 27017 |
| rentflow-kafka | 9092 / 29092 | 9092 / 29092 |
| rentflow-zipkin | 9411 | 9411 |
| rentflow-prometheus | 9090 | 9090 |
| rentflow-grafana | 3000 | 3000 |


### 1.4 Repo path

```text
$REPO_ROOT

```

### 1.5 Full stack status snapshot

```bash
# Git Bash (host)
docker ps --format "table {{.Names}}\t{{.Status}}" | grep rentflow

```

## 2. Health Checks

### 2.1 Full stack health

```bash
# Git Bash (host)
docker ps --format "table {{.Names}}\t{{.Status}}" | grep rentflow | sort

```

Expected: every line ends with (healthy).

Exception: notification-engine may show (unhealthy) for the first 3–4 minutes after up -d — this is normal. See 4.7.

### 2.2 Individual service health

```bash
# Git Bash (host)
curl -s http://localhost:8080/actuator/health      # gateway

curl -s http://localhost:8090/actuator/health      # auth

curl -s http://localhost:8082/actuator/health      # property

curl -s http://localhost:8085/actuator/health      # lease

curl -s http://localhost:8086/actuator/health      # payment

curl -s http://localhost:8089/actuator/health      # mpesa

curl -s http://localhost:8084/actuator/health      # maintenance

curl -s http://localhost:8081/actuator/health      # notification

```

Expected:

```json
{"status":"UP"}

```

### 2.3 Eureka registration

Open http://localhost:8761 in browser — all 9 services should appear with status UP.

Or query JSON:

```bash
# Git Bash (host)
curl -s http://localhost:8761/eureka/apps \

  -H "Accept: application/json" \

  | grep -o '"name":"[A-Z-]*"' \

  | sort -u

```

Expected 9 services:

```text
AUTH-SERVICE
GATEWAY-SERVICE
LEASE-SERVICE
MAINTENANCE-TICKET-SERVICE
MPESA-SERVICE
NOTIFICATION-ENGINE
PAYMENT-SERVICE
PROPERTY-MANAGEMENT-SERVICE
```

### 2.4 End-to-end smoke test

```bash
# Git Bash (host)
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \

  -H "Content-Type: application/json" \

  -d '{"email":"bob@test.com","password":"Passw0rd!"}' \

  | python -c "import sys,json; print(json.load(sys.stdin)['accessToken'])")

echo "Token length: ${#TOKEN}"

curl -s http://localhost:8080/api/v1/leases \

  -H "Authorization: Bearer $TOKEN" \

  -H "X-Tenant-ID: default" | head -c 100

echo

curl -s http://localhost:8080/api/v1/invoices \

  -H "Authorization: Bearer $TOKEN" \

  -H "X-Tenant-ID: default" | head -c 100

echo

curl -s http://localhost:8080/api/v1/tickets \

  -H "Authorization: Bearer $TOKEN" \

  -H "X-Tenant-ID: default" | head -c 100

echo

```

If any returns 401, the token expired (15-minute lifetime). Re-login.

## 3. Common Symptoms → Fixes

### 3.1 ERR_CONNECTION_RESET from frontend

Symptom: Browser console shows POST http://localhost:8080/... net::ERR_CONNECTION_RESET.

Diagnose:

```bash
# Git Bash (host)
docker ps --filter "name=rentflow-gateway" --format "{{.Status}}"

curl -s http://localhost:8080/actuator/health

```

Fix — if gateway is unhealthy or restarting:

```bash
# Git Bash (host)
docker compose restart gateway-service

sleep 30

curl -s http://localhost:8080/actuator/health

```

Fix — if health returns 503, a downstream service is down:

```bash
# Git Bash (host)
docker compose restart

sleep 60

```

### 3.2 401 Unauthorized on everything

Symptom: Every API call returns 401 even after logging in.

Diagnose:

Copy the tenant_token from browser localStorage, paste into jwt.io. Verify the payload contains:

```json
{

```

  "sub": "bob@test.com",

  "publicId": "c0d4c5dc-80a8-41d9-98d2-b7ec2ea3442a",

  "role": "TENANT"

}

Fixes by missing claim:

| Missing claim | Cause | Fix |
| --- | --- | --- |
| publicId | JWT builder missing it | Rebuild auth-service |
| role | JWT builder missing it | Rebuild auth-service |
| Token expired | 15-minute access lifetime | Log out and log back in |


Rebuild auth-service:

```bash
# Git Bash (host)
docker compose build --no-cache auth-service

docker compose up -d auth-service

```

### 3.3 403 Forbidden on specific endpoint

Symptom: Some endpoints work, one returns 403.

Diagnose:

The endpoint likely requires a role you don't have. For example, /api/v1/leases/{id}/approve requires LANDLORD.

Fix: Log in with the correct role.

Or check SecurityConfig in the target service for the path pattern.

### 3.4 404 Not Found through gateway

Symptom: Direct service call works; gateway call returns 404.

Diagnose:

```bash
# Git Bash (host)
grep "\\.path(" gateway-service/src/main/java/com/rentflow/gateway/GatewayApplication.java

```

Fix: Add the missing route and rebuild gateway:

```bash
# Git Bash (host)
docker compose build --no-cache gateway-service

docker compose up -d gateway-service

```

Common missed prefixes:

```text
/api/v1/invoices/**

```

/api/v1/tickets/\*\*

/api/v1/notifications/\*\*

### 3.5 503 Service Unavailable through gateway

Symptom: Gateway up, but a specific route returns 503.

Cause: Target service not registered in Eureka yet.

Fix:

```bash
# Git Bash (host)
curl -s http://localhost:8761/eureka/apps \

  -H "Accept: application/json" \

  | grep -i "SERVICE-NAME"

```

If missing, check the service logs:

```bash
# Git Bash (host)
docker logs --tail 30 rentflow-<service>

```

Wait 30–60 seconds. Eureka registration is eventually consistent.

### 3.6 500 Internal Server Error

Symptom: Any 500 response.

Diagnose — get the real exception (framework frames stripped):

```bash
# Git Bash (host)
docker logs --tail 100 rentflow-<service> 2>&1 \

  | grep -v '^[[:space:]]*at ' \

  | tail -30

```

Or find the summary line:

```bash
# Git Bash (host)
docker logs --tail 200 rentflow-<service> 2>&1 \

  | grep "threw exception" \

  | tail -3

```

Common 500 causes:

| Exception | Cause | Fix |
| --- | --- | --- |
| NoClassDefFoundError: org/xerial/snappy/SnappyOutputStream | snappy excluded from spring-kafka | Remove exclusions from pom.xml (see 4.6) |
| SerializationException: Can't convert key of class java.util.UUID | Kafka key is UUID, not String | Add .toString() before kafkaTemplate.send() |
| IllegalArgumentException: Invalid UUID string: default | Parsed X-Tenant-ID as UUID | Wrap in try/catch |
| DataIntegrityViolationException | NOT NULL or FK constraint | Check entity fields |


### 3.7 Kafka consumer retry loop

Symptom:

```text
Skipping record after retries: ... err=Listener method could not be invoked

```

Cause: Either the deserializer config mismatches the listener signature, OR two implementations of the same bean conflict.

Fix — for KafkaConfig classes:

```java
@Bean

```

public ConsumerFactory<String, String> consumerFactory() {

    Map<String, Object> props = new HashMap<>();

    // ...

    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,

              StringDeserializer.class);

    // NOT JsonDeserializer

    return new DefaultKafkaConsumerFactory<>(props);

}

And the listener:

```java
@KafkaListener(topics = "...", groupId = "...")

```

public void onEvent(String rawPayload, Acknowledgment ack) {

    // ...

}

If two KafkaConfig beans exist, delete the duplicate — Spring fails at startup:

```text
BeanDefinitionOverrideException: Invalid bean definition with name 'consumerFactory'

```

### 3.8 Notification engine UNKNOWN_TOPIC_OR_PARTITION

Symptom:

```text
Error while fetching metadata ... {lease-events=UNKNOWN_TOPIC_OR_PARTITION}

```

Cause: The consumer subscribed to topics that no publisher uses.

Fix: Remove stale topics from KafkaNotificationConsumer.java:

```java
@KafkaListener(

```

    topics = {

        "auth.user.events",

        "lease.lease.created",

        "lease.lease.approved",

        "lease.lease.terminated",

        "maintenance.ticket.created",

        "maintenance.ticket.resolved",

        "property.property.registered",

        "payment.events.received",

        "payment.events.failed",

        "payment.events.initiated",

        "mpesa.events.stk.requested",

        "mpesa.events.stk.result",

        "invoice.invoice.created"

    },

    groupId = "${spring.kafka.consumer.group-id}"

)

Do NOT include:

```text
lease-events

```

property-events

payment-events

lease-saga-replies

maintenance-events

Then rebuild:

```bash
# Git Bash (host)
docker compose build --no-cache notification-engine

docker compose up -d notification-engine

sleep 240

```

### 3.9 Notification dispatch skipped

Symptom:

```text
Routed event: type=INVOICE_CREATED tenant=c0d4c5dc-... landlord=null

```

Skipping dispatch — recipient UUID is null for event ...

Cause: Event payload lacks tenantId or landlordId.

Fix: Update the producer to include recipients. See ARCHITECTURE.md §7.

### 3.10 Duplicate key on refresh tokens

Symptom:

```text
could not execute statement [ERROR: duplicate key value violates unique constraint "ukghpmfn23vmxfu3spu3lfg4r2d"]

```

Cause: Two logins within the same second produce identical JWT strings (same iat).

Fix: JWT must include a random jti:

```java
Jwts.builder()

```

    .id(UUID.randomUUID().toString())   // ← jti

    .subject(userDetails.getUsername())

    // ...

And RefreshTokenService should revoke all prior tokens:

```java
List<RefreshToken> existing =

```

    refreshTokenRepository.findAllByUserAndRevokedFalse(user);

existing.forEach(rt -> rt.setRevoked(true));

refreshTokenRepository.saveAll(existing);

Quick DB fix:

```bash
# Git Bash (host)
MSYS_NO_PATHCONV=1 docker exec rentflow-postgres psql -U rentflow -d rentflow \

  -c "DELETE FROM refresh_tokens;"

```

## 4. Service-Specific Runbooks

### 4.1 Auth Service

Restart:

```bash
# Git Bash (host)
docker compose build --no-cache auth-service

docker compose up -d auth-service

sleep 30

curl -s http://localhost:8090/actuator/health

```

Clear stuck refresh tokens:

```bash
# Git Bash (host)
MSYS_NO_PATHCONV=1 docker exec rentflow-postgres psql -U rentflow -d rentflow \

  -c "DELETE FROM refresh_tokens;"

```

Reset a user's password (dev only):

```bash
# Git Bash (host)
MSYS_NO_PATHCONV=1 docker exec rentflow-postgres psql -U rentflow -d rentflow \

  -c "UPDATE users SET password = (SELECT password FROM users WHERE email='alice@test.com') WHERE email='bob@test.com';"

```

### 4.2 Property Service

Common issue: Invalid UUID string: default on POST /properties.

Fix: PropertyServiceImpl must guard tenant header parsing:

```java
UUID tenantUuid = null;

```

if (tenantHeader != null && !tenantHeader.isBlank()) {

    try {

        tenantUuid = UUID.fromString(tenantHeader);

    } catch (IllegalArgumentException e) {

        log.warn("Ignoring non-UUID X-Tenant-ID header: {}", tenantHeader);

    }

}

Unit creation missing unitNumber:

UnitCreateRequest has @NotBlank on unitNumber — remove it and auto-generate in UnitServiceImpl:

```java
String unitNumber = request.getUnitNumber();

```

if (unitNumber == null || unitNumber.isBlank()) {

    long existing = unitRepository.findByPropertyId(property.getId()).size();

    unitNumber = String.valueOf((char) ('A' + existing));

}

### 4.3 Lease Service

Restart:

```bash
# Git Bash (host)
docker compose build --no-cache lease-service

docker compose up -d lease-service

```

Common issue: BeanDefinitionOverrideException for LeaseEventProducer.

Cause: Two lease service implementations or two producers exist.

Fix:

```bash
# Git Bash (host)
find lease-service/src/main/java -name "**EventProducer**.java"

```

Delete any legacy LeaseEventProducer if a new one exists, OR mark one with @Primary.

### 4.4 Payment Service

Common issue: NoClassDefFoundError: org/xerial/snappy/SnappyOutputStream.

Cause: snappy-java excluded from spring-kafka in pom.xml.

Fix: See 4.6.

Invoice returns 500 with no recipients:

```bash
# Git Bash (host)
docker logs rentflow-payment | grep "Failed to publish InvoiceCreatedEvent"

```

Cross-service lookup to lease-service failing. Verify:

```bash
# Git Bash (host)
docker exec rentflow-payment sh -c \

  'wget -q -O - http://lease-service:8081/actuator/health'

```

Should return:

```json
{"status":"UP"}

```

If it fails, lease-service isn't reachable. Check services.lease.url in application.properties.

### 4.5 M-Pesa Service

STK push fails silently:

```bash
# Git Bash (host)
docker logs --tail 30 rentflow-mpesa | grep -iE "safaricom|error|rejected"

```

Look for:

| Error in log | Meaning |
| --- | --- |
| Safaricom STK push rejected: status=400 body=... | Credentials or request body issue |
| INVALID_ACCESS_TOKEN | Sandbox OAuth expired |
| Receiver not whitelisted | Phone number not approved |


Callback never arrives:

```bash
# Git Bash (host)
docker exec rentflow-mpesa env | grep DARAJA_CALLBACK_URL

```

If the URL doesn't match current ngrok tunnel, update .env and restart:

```bash
# Git Bash (host)
docker compose up -d --force-recreate mpesa-service

```

### 4.6 Snappy Class Not Found (Runtime Error)

Symptom:

```text
java.lang.NoClassDefFoundError: org/xerial/snappy/SnappyOutputStream

```

Cause: spring-kafka dependency in pom.xml excludes snappy, lz4, zstd. Kafka's producer eagerly loads compression codecs at first send().

Fix — remove exclusions from pom.xml.

Before (broken):

```xml
<dependency>

```

    <groupId>org.springframework.kafka</groupId>

    <artifactId>spring-kafka</artifactId>

    <exclusions>

        <exclusion>

            <groupId>org.xerial.snappy</groupId>

            <artifactId>snappy-java</artifactId>

        </exclusion>

        <!-- ... -->

    </exclusions>

</dependency>

After (working):

```xml
<dependency>

```

    <groupId>org.springframework.kafka</groupId>

    <artifactId>spring-kafka</artifactId>

</dependency>

Check across all services:

```bash
# Git Bash (host)
grep -rn -E 'snappy|lz4|zstd' */pom.xml

```

Any hits → remove. Then rebuild the affected service.

### 4.7 Notification Engine Slow Boot

Symptom: (unhealthy) status for 3–4 minutes after up -d.

Cause: Notification engine waits on Eureka registration plus Kafka topic metadata plus MongoDB connection. Boot takes approximately 55 seconds on good hardware, up to 4 minutes on slower machines.

Do: Wait it out. Check progress:

```bash
# Git Bash (host)
docker logs -f rentflow-notification

```

Look for:

```text
Started NotificationEngineApplication in NN.NNN seconds

```

Don't: Restart repeatedly. Each restart resets the clock.

If it never boots (5+ minutes):

Check for compile or runtime errors:

```bash
# Git Bash (host)
docker logs --tail 100 rentflow-notification 2>&1 \

  | grep -v '^[[:space:]]*at ' \

  | tail -30

```

Check MongoDB:

```bash
# Git Bash (host)
docker exec rentflow-mongodb mongosh --quiet --eval "db.adminCommand('ping')"

```

Check Kafka:

```bash
# Git Bash (host)
docker exec rentflow-kafka /opt/kafka/bin/kafka-topics.sh \

  --bootstrap-server kafka:29092 --list | head

```

### 4.8 Maintenance Service

Common issue: landlordId is required when a tenant creates a ticket.

Cause: Frontend doesn't send landlordId in the ticket payload.

Fix: Ensure TicketCreateRequest has landlordId, and TicketCommandServiceImpl resolves it. Or make the frontend send it from the active lease's property.

## 5. Database Operations

### 5.1 PostgreSQL — Common Queries

All users:

```bash
# Git Bash (host)
MSYS_NO_PATHCONV=1 docker exec rentflow-postgres psql -U rentflow -d rentflow \

  -c "SELECT id, public_id, email, role FROM users;"

```

Active leases:

```bash
# Git Bash (host)
MSYS_NO_PATHCONV=1 docker exec rentflow-postgres psql -U rentflow -d rentflow \

  -c "SELECT id, tenant_id, landlord_id, status, rent_amount FROM leases WHERE status='ACTIVE';"

```

Recent invoices:

```bash
# Git Bash (host)
MSYS_NO_PATHCONV=1 docker exec rentflow-postgres psql -U rentflow -d rentflow \

  -c "SELECT invoice_number, total_amount, paid_amount, status FROM invoices ORDER BY created_at DESC LIMIT 5;"

```

Recent M-Pesa transactions:

```bash
# Git Bash (host)
MSYS_NO_PATHCONV=1 docker exec rentflow-postgres psql -U rentflow -d rentflow \

  -c "SELECT checkout_request_id, status, amount, mpesa_receipt_number FROM transactions ORDER BY created_at DESC LIMIT 5;"

```

Maintenance tickets:

```bash
# Git Bash (host)
MSYS_NO_PATHCONV=1 docker exec rentflow-postgres psql -U rentflow -d rentflow \

  -c "SELECT id, tenant_id, landlord_id, title, status FROM tickets ORDER BY created_at DESC LIMIT 5;"

```

Security deposits:

```bash
# Git Bash (host)
MSYS_NO_PATHCONV=1 docker exec rentflow-postgres psql -U rentflow -d rentflow \

  -c "SELECT id, lease_id, total_deposit, current_balance, deduction_total, return_amount, returned_date FROM security_deposits;"

```

Table list:

```bash
# Git Bash (host)
MSYS_NO_PATHCONV=1 docker exec rentflow-postgres psql -U rentflow -d rentflow -c "\dt"

```

Column names for a specific table:

```bash
# Git Bash (host)
MSYS_NO_PATHCONV=1 docker exec rentflow-postgres psql -U rentflow -d rentflow \

  -c "SELECT column_name, data_type FROM information_schema.columns WHERE table_name='users' ORDER BY ordinal_position;"

```

### 5.2 PostgreSQL — Reset Operations (Dev Only)

> **DEV ONLY. Do not run in production.** These commands delete or overwrite application data.

Delete all leases (unblocks re-apply):

```bash
# Git Bash (host)
MSYS_NO_PATHCONV=1 docker exec rentflow-postgres psql -U rentflow -d rentflow \

  -c "DELETE FROM leases;"

```

Reset units to available:

```bash
# Git Bash (host)
MSYS_NO_PATHCONV=1 docker exec rentflow-postgres psql -U rentflow -d rentflow \

  -c "UPDATE units SET status='AVAILABLE', current_tenant_id=NULL;"

```

Wipe invoices and line items:

```bash
# Git Bash (host)
MSYS_NO_PATHCONV=1 docker exec rentflow-postgres psql -U rentflow -d rentflow \

  -c "DELETE FROM invoice_line_items; DELETE FROM invoices;"

```

Wipe payments and splits:

```bash
# Git Bash (host)
MSYS_NO_PATHCONV=1 docker exec rentflow-postgres psql -U rentflow -d rentflow \

  -c "DELETE FROM payment_splits; DELETE FROM payments;"

```

Wipe M-Pesa transactions:

```bash
# Git Bash (host)
MSYS_NO_PATHCONV=1 docker exec rentflow-postgres psql -U rentflow -d rentflow \

  -c "DELETE FROM transactions;"

```

Clear refresh tokens:

```bash
# Git Bash (host)
MSYS_NO_PATHCONV=1 docker exec rentflow-postgres psql -U rentflow -d rentflow \

  -c "DELETE FROM refresh_tokens;"

```

Copy Alice's password hash to Bob:

```bash
# Git Bash (host)
MSYS_NO_PATHCONV=1 docker exec rentflow-postgres psql -U rentflow -d rentflow \

  -c "UPDATE users SET password = (SELECT password FROM users WHERE email='alice@test.com') WHERE email='bob@test.com';"

```

### 5.3 Backup & Restore

Full database dump:

```bash
# Git Bash (host)
MSYS_NO_PATHCONV=1 docker exec rentflow-postgres pg_dump -U rentflow rentflow \

  > /tmp/rentflow_backup_$(date +%Y%m%d_%H%M%S).sql

```

ls -lh /tmp/rentflow_backup_\*.sql

Restore from backup:

```bash
# Git Bash (host)
MSYS_NO_PATHCONV=1 docker exec -i rentflow-postgres psql -U rentflow -d rentflow \

  < /tmp/rentflow_backup_20261003_120000.sql

```

### 5.4 MongoDB — Common Queries

List databases:

```bash
# Git Bash (host)
docker exec rentflow-mongodb mongosh --quiet \

  --eval "db.adminCommand('listDatabases').databases.forEach(d => print(d.name))"

```

Notification log count:

```bash
# Git Bash (host)
docker exec rentflow-mongodb mongosh --quiet notificationdb \

  --eval 'db.notification_logs.countDocuments()'

```

Distinct event types seen:

```bash
# Git Bash (host)
docker exec rentflow-mongodb mongosh --quiet notificationdb \

  --eval 'db.notification_logs.distinct("eventType")'

```

Counts per event type:

```bash
# Git Bash (host)
docker exec rentflow-mongodb mongosh --quiet notificationdb \

  --eval 'db.notification_logs.aggregate([{$group:{_id:"$eventType",count:{$sum:1}}},{$sort:{count:-1}}]).toArray()'

```

User preferences:

```bash
# Git Bash (host)
docker exec rentflow-mongodb mongosh --quiet notificationdb \

  --eval 'db.user_preferences.find().toArray()'

```

Recent 5 notifications:

```bash
# Git Bash (host)
docker exec rentflow-mongodb mongosh --quiet notificationdb \

  --eval 'db.notification_logs.find().sort({createdAt:-1}).limit(5).toArray()'

```

Count notifications for a specific event type:

```bash
# Git Bash (host)
docker exec rentflow-mongodb mongosh --quiet notificationdb \

  --eval 'db.notification_logs.countDocuments({eventType:"LEASE_CREATED"})'

```

> **DEV ONLY. Do not run in production.**

Wipe notification logs (dev):

```bash
# Git Bash (host)
docker exec rentflow-mongodb mongosh --quiet notificationdb \

  --eval 'db.notification_logs.deleteMany({})'

```

> **DEV ONLY. Do not run in production.**

Wipe preferences (dev):

```bash
# Git Bash (host)
docker exec rentflow-mongodb mongosh --quiet notificationdb \

  --eval 'db.user_preferences.deleteMany({})'

```

Note: In Git Bash, use single quotes around the --eval argument to prevent $ mangling.

## 6. Kafka Operations

### 6.1 List all topics

```bash
# Git Bash (host)
docker exec rentflow-kafka /opt/kafka/bin/kafka-topics.sh \

  --bootstrap-server kafka:29092 --list

```

### 6.2 Topic offsets (message counts)

```bash
# Git Bash (host)
docker exec rentflow-kafka /opt/kafka/bin/kafka-get-offsets.sh \

  --bootstrap-server kafka:29092

```

### 6.3 Consumer group status

```bash
# Git Bash (host)
docker exec rentflow-kafka /opt/kafka/bin/kafka-consumer-groups.sh \

  --bootstrap-server kafka:29092 \

  --describe --group notification-engine-group

```

Look at the LAG column. A growing lag means the consumer is failing.

### 6.4 Publish a test event

Useful for testing the notification engine without triggering a full workflow:

```bash
# Git Bash (host)
echo '{"eventType":"lease.lease.created","leaseId":"00000000-0000-0000-0000-000000000001","tenantId":"c0d4c5dc-80a8-41d9-98d2-b7ec2ea3442a","landlordId":"07767213-612b-4f0b-987c-75e43e5ca152","startDate":"2026-10-01","endDate":"2027-10-01","monthlyRent":24000,"correlationId":"00000000-0000-0000-0000-000000000099"}' \

  | docker exec -i rentflow-kafka /opt/kafka/bin/kafka-console-producer.sh \

      --bootstrap-server kafka:29092 \

      --topic lease.lease.created

```

Watch the notification engine consume it:

```bash
# Git Bash (host)
docker logs -f rentflow-notification | grep "Routed event"

```

### 6.5 Read topic messages

```bash
# Git Bash (host)
docker exec rentflow-kafka /opt/kafka/bin/kafka-console-consumer.sh \

  --bootstrap-server kafka:29092 \

  --topic lease.lease.created \

  --from-beginning \

  --max-messages 5 \

  --timeout-ms 5000

```

### 6.6 Reset consumer group offset

> **DEV ONLY. Do not run in production.** This changes Kafka consumer offsets and can replay or skip messages.

Warning: Dev only. Resets the consumer's position to replay or skip messages.

Replay all messages:

```bash
# Git Bash (host)
docker exec rentflow-kafka /opt/kafka/bin/kafka-consumer-groups.sh \

  --bootstrap-server kafka:29092 \

  --group notification-engine-group \

  --topic lease.lease.created \

  --reset-offsets --to-earliest --execute

```

Skip to latest:

```bash
# Git Bash (host)
docker exec rentflow-kafka /opt/kafka/bin/kafka-consumer-groups.sh \

  --bootstrap-server kafka:29092 \

  --group notification-engine-group \

  --topic lease.lease.created \

  --reset-offsets --to-latest --execute

```

Consumer must be stopped first.

### 6.7 Delete a topic

> **DEV ONLY. Do not run in production.** Topic deletion is destructive and may permanently remove event history.

```bash
# Git Bash (host)
docker exec rentflow-kafka /opt/kafka/bin/kafka-topics.sh \

  --bootstrap-server kafka:29092 \

  --delete --topic <topic-name>

```

## 7. Frontend Operations

### 7.1 Kill Vite port and restart (CMD)

```cmd
REM CMD (Windows host)
for %p in (5173 5174) do @for /f "tokens=5" %a in ('netstat -ano ^| findstr :%p ^| findstr LISTENING') do @taskkill /F /PID %a 2>nul

```

Then:

```cmd
REM CMD (Windows host)
cd /d "$REPO_ROOT\\.github\workflows\tenant-frontend"

rmdir /s /q node_modules\\.vite

npm run dev

```

### 7.2 Clear browser stale state

Frontend caches JWT in localStorage. To force fresh:

F12 → Application → Local Storage → Clear

Hard refresh: Ctrl + Shift + R

Or open an incognito window.

### 7.3 Build for production

```cmd
REM CMD (Windows host)
cd .github\workflows\tenant-frontend

npm run build

```

Output goes to dist/. Nginx serves this.

### 7.4 Common TypeScript errors

Cannot find module '../../api/payment.api'

Payment API file deleted or renamed. Restore it. See src/api/payment.api.ts in git history.

'Lease' is declared locally but not exported

Wrong import: import { Lease } from '../../api/lease.api'

Correct: import type { Lease } from '../../types/lease'

Property 'createUnit' does not exist on type 'PropertyStore'

Store missing the action. Rebuild src/stores/property.ts.

### 7.5 Tailwind styles not applying

PostCSS caching issue. Clear Vite cache:

```cmd
REM CMD (Windows host)
rmdir /s /q node_modules\\.vite

npm run dev

```

## 8. Port & Process Management

### 8.1 Kill process on a port (CMD)

```cmd
REM CMD (Windows host)
for /f "tokens=5" %a in ('netstat -ano ^| findstr :5173 ^| findstr LISTENING') do taskkill /F /PID %a

```

Replace 5173 with any port.

### 8.2 Kill Vite (5173 + 5174) at once

```cmd
REM CMD (Windows host)
for %p in (5173 5174) do @for /f "tokens=5" %a in ('netstat -ano ^| findstr :%p ^| findstr LISTENING') do @taskkill /F /PID %a 2>nul

```

### 8.3 Kill ngrok web inspector (4040)

```cmd
REM CMD (Windows host)
for /f "tokens=5" %a in ('netstat -ano ^| findstr :4040 ^| findstr LISTENING') do taskkill /F /PID %a

```

### 8.4 Do NOT kill Docker ports

These ports are owned by com.docker.backend.exe or Docker Desktop. Killing them breaks all containers:

```text
8080  8081  8082  8084  8085  8086  8089  8761

```

5432  27017  9092  29092  9411  9090  3000

To restart a Docker service, use:

```bash
# Git Bash (host)
docker compose restart <service>

```

Or:

```bash
# Git Bash (host)
docker restart rentflow-<service>

```

### 8.5 Check what's on a port

```cmd
REM CMD (Windows host)
netstat -ano | findstr :5173

```

The last number is the PID. Kill it with:

```cmd
REM CMD (Windows host)
taskkill /F /PID <pid>

```

### 8.6 Kill all Java processes (nuclear)

> **DEV ONLY. Do not run in production.** This terminates every Java process on the Windows host.

```cmd
REM CMD (Windows host)
taskkill /F /IM java.exe

taskkill /F /IM node.exe

```

Useful when Docker isn't involved and you want a clean slate. But it kills Docker Desktop's embedded Java too — Docker will need a restart.

## 9. Common JAR Distribution

This is the #1 source of "why doesn't my code change take effect?"

Every service has libs/common-1.0.0-SNAPSHOT.jar copied during its Docker build. After modifying anything in common/, you must rebuild and redistribute.

### 9.1 Full rebuild + redistribute + rebuild affected services

```bash
# Git Bash (host)
cd "$REPO_ROOT"

# 1. Rebuild the JAR

./mvnw -pl common clean package -DskipTests

# 2. Redistribute to every service's libs/

find . -path "**/libs/common-1.0.0-SNAPSHOT.jar" -not -path "**/target/*" \

  -exec cp common/target/common-1.0.0-SNAPSHOT.jar {} +

# 3. Verify all copies have fresh timestamps

find . -path "*/libs/common-1.0.0-SNAPSHOT.jar" \

  -not -path "**/target/**" -exec ls -la {} +

# 4. Rebuild affected services

docker compose build --no-cache \

  auth-service \

  lease-service \

  payment-service \

  mpesa-service \

  property-management-service \

  maintainance-ticket-service \

  notification-engine

docker compose up -d

```

### 9.2 Verify a JAR has the expected class or fields

```bash
# Git Bash (host)
cd /tmp

rm -rf check && mkdir check && cd check

MSYS_NO_PATHCONV=1 jar xf "$REPO_ROOT/lease-service/libs/common-1.0.0-SNAPSHOT.jar" \

  com/platform/common/events/lease/LeaseCreatedEvent.class

javap -p com/platform/common/events/lease/LeaseCreatedEvent.class

```

Expected output shows fields plus getters and setters. If the new field isn't listed, the JAR is stale.

### 9.3 Save as a reusable script

Create `rebuild-common.sh` at the repo root:

```bash
# Git Bash (host)
#!/bin/bash
set -e

echo "Building common JAR..."
./mvnw -pl common clean package -DskipTests

echo "Distributing to services..."
find . -path "*/libs/common-1.0.0-SNAPSHOT.jar" -not -path "**/target/**" \
  -exec cp common/target/common-1.0.0-SNAPSHOT.jar {} +

echo "Done. Now run: docker compose build --no-cache <services> && docker compose up -d"
```

Run with:

```bash
# Git Bash (host)
bash rebuild-common.sh
```

## 10. M-Pesa & Safaricom Operations

### 10.1 Start ngrok tunnel

```cmd
REM CMD (Windows host)
ngrok http 8089

```

Copy the Forwarding HTTPS URL.

### 10.2 Update callback URL

Edit .env:

```text
DARAJA_CALLBACK_URL=https://<your-subdomain>.ngrok-free.dev/api/v1/mpesa/callback

```

Restart mpesa-service:

```bash
# Git Bash (host)
docker compose up -d --force-recreate mpesa-service

sleep 20

docker exec rentflow-mpesa env | grep DARAJA_CALLBACK_URL

```

Verify env is set correctly.

### 10.3 Check ngrok tunnel status

```bash
# Git Bash (host)
curl -s http://127.0.0.1:4040/api/tunnels | python -m json.tool

```

Or open http://127.0.0.1:4040 in browser to see request log.

### 10.4 Test STK push manually

## 1. Get tenant token:

```bash
# Git Bash (host)
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \

  -H "Content-Type: application/json" \

  -d '{"email":"bob@test.com","password":"Passw0rd!"}' \

  | python -c "import sys,json; print(json.load(sys.stdin)['accessToken'])")

```

## 2. Send STK push with sandbox test MSISDN:

```bash
# Git Bash (host)
curl -i -X POST http://localhost:8080/api/v1/mpesa/stk-push \

  -H "Authorization: Bearer $TOKEN" \

  -H "X-Tenant-ID: default" \

  -H "Content-Type: application/json" \

  -d '{

    "phone":"254708374149",

    "amount":1,

    "leaseId":"45c03bf9-7364-45b4-963f-a39794d43253",

    "tenantId":"c0d4c5dc-80a8-41d9-98d2-b7ec2ea3442a",

    "accountReference":"TEST",

    "transactionDesc":"Test payment"

  }'

```

Expected: 200 OK with checkoutRequestId.

### 10.5 Common STK push errors

| Error in log | Cause | Fix |
| --- | --- | --- |
| 400 Bad Request [no body] on OAuth | Empty consumer key or secret | Check .env DARAJA_\* variables |
| Receiver not whitelisted | Phone not approved in Safaricom portal | Use 254708374149 |
| Invalid Access Token | Sandbox OAuth expired | Retry — new token is fetched per request |
| 9999 Error occurred while sending push request | Sandbox flakiness | Retry 3–5 times, or wait 10 minutes |
| Callback never arrives | ngrok down or URL stale | Restart ngrok and update .env |


### 10.6 Manually mark a transaction as paid (test)

> **DEV ONLY. Do not run in production.** This directly changes payment state and does not execute the normal callback flow.

```bash
# Git Bash (host)
MSYS_NO_PATHCONV=1 docker exec rentflow-postgres psql -U rentflow -d rentflow \

  -c "UPDATE transactions SET status='SUCCESS', mpesa_receipt_number='QKTEST123' WHERE checkout_request_id='ws_CO_...' RETURNING id, status;"

```

Does NOT trigger the event — only a real callback does.

### 10.7 Safaricom sandbox test MSISDN

Use:

```text
254708374149

```

This is the standard sandbox number that auto-completes without whitelisting.

Real phone numbers require approval via apisupport@safaricom.co.ke.

## 11. Emergency Recovery

### 11.1 Restart the entire stack

```bash
# Git Bash (host)
cd "$REPO_ROOT"

docker compose down

docker compose up -d

sleep 90

docker ps --format "table {{.Names}}\t{{.Status}}" | grep rentflow

```

Wait 90–120 seconds for all services to register in Eureka.

### 11.2 Restart just one service

```bash
# Git Bash (host)
docker compose restart <service-name>

```

Or with rebuild:

```bash
# Git Bash (host)
docker compose build --no-cache <service-name>

docker compose up -d <service-name>

sleep 30

```

### 11.3 Wipe all data and start fresh

> **DEV ONLY. Do not run in production.** This removes Docker volumes and destroys local application data.

Destroys everything.

```bash
# Git Bash (host)
cd "$REPO_ROOT"

docker compose down -v        # -v removes volumes

docker compose up -d

sleep 120

```

Then re-register landlord and tenant:

Alice: alice@test.com / Passw0rd!

Bob: bob@test.com / Passw0rd!

### 11.4 Recover from stuck unhealthy services

If a service shows (unhealthy) for 5+ minutes:

```bash
# Git Bash (host)
# 1. Check logs

docker logs --tail 50 rentflow-<service>

# 2. If bean or config error, rebuild

docker compose build --no-cache <service>

docker compose up -d <service>

# 3. If still broken, remove the container

docker compose stop <service>

docker compose rm -f <service>

docker compose up -d <service>

```

### 11.5 Recover stuck Kafka consumer

```bash
# Git Bash (host)
# 1. Check consumer group

docker exec rentflow-kafka /opt/kafka/bin/kafka-consumer-groups.sh \

  --bootstrap-server kafka:29092 \

  --describe --group notification-engine-group

# 2. If LAG grows without consuming, restart the consumer

docker compose restart notification-engine

sleep 240

```

## 12. Log Collection for Debugging

### 12.1 Tail a specific service

```bash
# Git Bash (host)
docker logs -f rentflow-<service>

```

### 12.2 Filter to real error messages (strip stack frames)

```bash
# Git Bash (host)
docker logs --tail 200 rentflow-<service> 2>&1 \

  | grep -v '^[[:space:]]*at ' \

  | tail -30

```

### 12.3 Find the Spring summary line

```bash
# Git Bash (host)
docker logs --tail 200 rentflow-<service> 2>&1 \

  | grep "threw exception" \

  | tail -3

```

### 12.4 Save full log to file

```bash
# Git Bash (host)
docker logs rentflow-<service> > /tmp/<service>_$(date +%Y%m%d_%H%M%S).log 2>&1

```

### 12.5 Grep across all services at once

```bash
# Git Bash (host)
for svc in auth gateway property lease payment mpesa maintenance notification; do

  echo "=== $svc ==="

  docker logs --tail 30 rentflow-$svc 2>&1 \

    | grep -iE "error|exception|warn" \

    | tail -5

done

```

### 12.6 Watch for a specific traceId across services

Every log line includes [service-name,traceId,spanId]. Extract traceId from a frontend request's X-Request-ID response header, then:

```bash
# Git Bash (host)
for svc in auth gateway property lease payment mpesa maintenance notification; do

  out=$(docker logs --tail 200 rentflow-$svc 2>&1 | grep "<paste-trace-id>")

  if [ -n "$out" ]; then echo "=== $svc ==="; echo "$out"; fi

done

```

Great for following a request through the whole stack.

### 12.7 Check for disk-hogging log output

```bash
# Git Bash (host)
docker logs rentflow-notification 2>&1 | wc -l

docker logs rentflow-kafka 2>&1 | wc -l

docker logs rentflow-postgres 2>&1 | wc -l

```

If any exceed 100,000 lines, consider restarting. Log rotation may not be configured.

## Quick Copy-Paste Block — Most Common Fixes

```bash
# Git Bash (host)
# 1. Full stack status
docker ps --format "table {{.Names}}\t{{.Status}}" | grep rentflow

# 2. Gateway health
curl -s http://localhost:8080/actuator/health

# 3. Restart gateway + auth
docker compose restart gateway-service auth-service
sleep 30

# 4. Find the real exception (no stack frames)
docker logs --tail 100 rentflow-<service> 2>&1 | grep -v '^[[:space:]]*at ' | tail -20

# 5. After common/ change — rebuild and redistribute
./mvnw -pl common clean package -DskipTests
find . -path "*/libs/common-1.0.0-SNAPSHOT.jar" -not -path "**/target/**" \
  -exec cp common/target/common-1.0.0-SNAPSHOT.jar {} +
docker compose build --no-cache <affected-services>
docker compose up -d
```

### Frontend port cleanup and restart (CMD)

```cmd
REM CMD (Windows host)
for %p in (5173 5174) do @for /f "tokens=5" %a in ('netstat -ano ^| findstr :%p ^| findstr LISTENING') do @taskkill /F /PID %a 2>nul
cd /d "%USERPROFILE%\Downloads\tenant-landlord-system\tenant-landlord-system\.github\workflows\tenant-frontend"
rmdir /s /q node_modules\.vite
npm run dev
```

## Escalation Path

For issues not covered here:

Check Eureka: http://localhost:8761 — are all services registered?

Check Zipkin: http://localhost:9411 — find the failing trace

Check Grafana: http://localhost:3000 — error rate, latency, Kafka lag

Check the database — most 500 errors are constraint violations

Read the exception:

```bash
# Git Bash (host)
docker logs --tail 200 rentflow-<service> 2>&1 | grep -v '^[[:space:]]*at ' | tail -30

```

For Safaricom-specific issues, email apisupport@safaricom.co.ke with your Consumer Key and the exact errorMessage.

