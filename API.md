# RentFlow — API Reference

Complete REST API for the RentFlow platform.

All requests go through the gateway at `http://localhost:8080`. Every endpoint requires authentication unless marked **Public**.

---

## Table of Contents

1. Getting Started in 30 Seconds
2. Conventions
3. Authentication
4. Auth Service
5. Property Management
6. Lease Service
7. Payment Service — Invoices
8. Payment Service — Invoice Line Items
9. Payment Service — Security Deposits
10. Payment Service — Payments
11. Payment Service — Payment Splits
12. Payment Service — Payment Methods & Accounts
13. Payment Service — Audit Logs
14. M-Pesa Service
15. Maintenance Service
16. Notification Engine
17. Gateway Route Table
18. Kafka Event Reference
19. Error Code Reference
20. Versioning & Rate Limits
21. Complete Test Sequence
22. Appendix — Response Objects

---

# 1. Getting Started in 30 Seconds

### 1. Login as tenant

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"bob@test.com","password":"Passw0rd!"}' \
  | python -c "import sys,json; print(json.load(sys.stdin)['accessToken'])")

echo "Token length: ${#TOKEN}"
```

### 2. Make any authenticated call

```bash
curl -s http://localhost:8080/api/v1/leases \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: default" \
  | python -m json.tool
```

### Test credentials

| Role     | Email            | Password    |
| -------- | ---------------- | ----------- |
| Tenant   | `bob@test.com`   | `Passw0rd!` |
| Landlord | `alice@test.com` | `Passw0rd!` |
| Admin    | *(none seeded)*  | —           |

Access tokens expire in **15 minutes**.

Refresh tokens expire in **7 days**.

Re-login if you receive `401 Unauthorized`.

---

# 2. Conventions

## 2.1 Base URL

```text
http://localhost:8080
```

## 2.2 Required Headers

| Header          | Value                    | Source                  |
| --------------- | ------------------------ | ----------------------- |
| `Authorization` | `Bearer <accessToken>`   | Login response          |
| `Content-Type`  | `application/json`       | POST/PUT/PATCH requests |
| `X-Tenant-ID`   | `default`                | Static in development   |
| `X-User-Id`     | Auto-injected by gateway | JWT `publicId`          |
| `X-User-Role`   | Auto-injected by gateway | JWT `role`              |

> **Important:** Do not set `X-User-Id` or `X-User-Role` manually. The gateway strips incoming values and injects the authenticated user's identity from the JWT.

## 2.3 Response Format

### Success — single object

Responses return the object directly:

```json
{
  "id": "uuid",
  "status": "ACTIVE"
}
```

### Success — paginated

Most list endpoints use Spring's default pagination format:

```json
{
  "content": [],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 42,
  "totalPages": 3,
  "number": 0,
  "size": 20,
  "first": true,
  "last": false
}
```

### Lease pagination exception

The Lease Service uses a custom pagination shape:

```json
{
  "leases": [],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

> The array key is `leases`, not `content`. Other paginated list endpoints use Spring's default `content` format.

### Error

```json
{
  "timestamp": "2026-10-03T12:34:56.789Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed: {amount=must be greater than 0}",
  "path": "/api/v1/invoices"
}
```

## 2.4 Status Codes

| Code  | Meaning                          |
| ----- | -------------------------------- |
| `200` | OK                               |
| `201` | Created                          |
| `204` | No Content                       |
| `400` | Validation error                 |
| `401` | Missing/invalid JWT              |
| `403` | Role not permitted               |
| `404` | Resource not found               |
| `409` | Conflict                         |
| `500` | Server error                     |
| `503` | Service not registered in Eureka |

## 2.5 Pagination

List endpoints accept:

```text
?page=0&size=20&sort=createdAt,desc
```

Default page size is `20`.

Maximum page size is typically `100`.

## 2.6 Role Access Matrix

| Endpoint prefix                 | TENANT   | LANDLORD | ADMIN |
| ------------------------------- | -------- | -------- | ----- |
| `/api/v1/auth/**`               | ✅        | ✅        | ✅     |
| `/api/v1/properties/**`         | Read     | CRUD     | Read  |
| `/api/v1/landlords/**`          | Read     | CRUD     | Read  |
| `/api/v1/units/**`              | Read     | CRUD     | Read  |
| `/api/v1/leases/**`             | Own only | Own only | All   |
| `/api/v1/invoices/**`           | Own only | Own only | All   |
| `/api/v1/invoice-line-items/**` | Read     | CRUD     | Read  |
| `/api/v1/security-deposits/**`  | Own only | Own only | All   |
| `/api/v1/payments/**`           | Read     | Read     | CRUD  |
| `/api/v1/payment-splits/**`     | Read     | CRUD     | Read  |
| `/api/v1/mpesa/**`              | Initiate | —        | —     |
| `/api/v1/tickets/**`            | Own only | Own only | All   |
| `/api/v1/notifications/**`      | Own only | Own only | All   |

**Own only** means resources are automatically scoped by `X-User-Id`.

* TENANT → resources belonging to the tenant
* LANDLORD → resources belonging to the landlord
* ADMIN → unrestricted access

## 2.7 Common Error Scenarios

| What you see                       | Why                                     | Fix                        |
| ---------------------------------- | --------------------------------------- | -------------------------- |
| `401 Unauthorized`                 | Access token expired                    | Re-login                   |
| `403 Forbidden`                    | Wrong role                              | Log in as the correct role |
| `404 Not Found` through gateway    | Route prefix not registered             | Check Gateway Route Table  |
| `503 Service Unavailable`          | Target service not registered in Eureka | Wait 30–60 seconds         |
| Empty `/leases` list               | Auto-scoping; caller has no leases      | Verify caller identity     |
| `landlordId` differs between calls | It is the user's `publicId`             | Use `publicId` everywhere  |

---

# 3. Authentication

## Login

```http
POST /api/v1/auth/login
Content-Type: application/json
```

### Request

```json
{
  "email": "bob@test.com",
  "password": "Passw0rd!"
}
```

### Response — `200 OK`

```json
{
  "id": "c0d4c5dc-80a8-41d9-98d2-b7ec2ea3442a",
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzM4NCJ9...",
  "role": "TENANT",
  "email": "bob@test.com"
}
```

### JWT Payload

```json
{
  "jti": "bc7dc253-04f2-4d59-9de7-a712fc45dce1",
  "sub": "bob@test.com",
  "publicId": "c0d4c5dc-80a8-41d9-98d2-b7ec2ea3442a",
  "role": "TENANT",
  "iat": 1790000000,
  "exp": 1790000900
}
```

### Errors

| Status | When                   |
| ------ | ---------------------- |
| `400`  | Missing email/password |
| `401`  | Invalid credentials    |
| `403`  | Account disabled       |

---

## Register

```http
POST /api/v1/auth/register
Content-Type: application/json
```

### Request

```json
{
  "email": "newuser@test.com",
  "password": "Passw0rd!",
  "fullName": "New User",
  "phone": "254700000001",
  "role": "TENANT"
}
```

`role` must be one of:

```text
TENANT
LANDLORD
ADMIN
```

### Response

`201 Created`

Same shape as the login response.

### Errors

| Status | When                           |
| ------ | ------------------------------ |
| `400`  | Missing field or weak password |
| `409`  | Email already registered       |

---

## Refresh Access Token

```http
POST /api/v1/auth/refresh
Content-Type: application/json
```

### Request

```json
{
  "refreshToken": "eyJhbGciOiJIUzM4NCJ9..."
}
```

### Response

`200 OK`

Same shape as login with a new access and refresh token.

The old refresh token is revoked after use.

### Errors

| Status | When                     |
| ------ | ------------------------ |
| `400`  | Missing token            |
| `401`  | Revoked or expired token |

---

# 4. Auth Service

Base path:

```text
/api/v1/auth
```

| Method | Path        | Public | Description               |
| ------ | ----------- | ------ | ------------------------- |
| POST   | `/login`    | ✅      | Get JWT                   |
| POST   | `/register` | ✅      | Create account            |
| POST   | `/refresh`  | ✅      | Rotate tokens             |
| POST   | `/callback` | ✅      | OAuth callback — reserved |

No user-lookup endpoint exists yet.

Cross-service callers use the `publicId` from the JWT.

---

# 5. Property Management

Base paths:

```text
/api/v1/properties
/api/v1/landlords
/api/v1/units
```

## 5.1 Properties

| Method | Path                                   | Roles    | Description                  |
| ------ | -------------------------------------- | -------- | ---------------------------- |
| GET    | `/properties`                          | Any      | List all properties          |
| GET    | `/properties/{id}`                     | Any      | Get by UUID                  |
| GET    | `/properties/property-id/{businessId}` | Any      | Get by external business ID  |
| GET    | `/properties/landlord/{landlordId}`    | LANDLORD | Properties for landlord      |
| GET    | `/properties/status/{status}`          | Any      | Filter by status             |
| GET    | `/properties/city/{city}`              | Any      | Filter by city               |
| POST   | `/properties`                          | LANDLORD | Create property + first unit |
| PUT    | `/properties/{id}`                     | LANDLORD | Update                       |
| PATCH  | `/properties/{id}/status`              | LANDLORD | Change status                |
| DELETE | `/properties/{id}`                     | LANDLORD | Delete                       |

### Create Property

```http
POST /api/v1/properties
Authorization: Bearer <landlord-token>
Content-Type: application/json
```

### Request

```json
{
  "propertyId": "11111111-2222-3333-4444-555555555555",
  "addressLine1": "Alice Apartments",
  "addressLine2": null,
  "city": "Mombasa",
  "state": null,
  "zipCode": null,
  "country": "Kenya"
}
```

`landlordId` is optional and is resolved from `X-User-Id`.

### Response — `201 Created`

```json
{
  "id": "f37c55b5-1057-459d-825c-3fac95dd71bc",
  "propertyId": "11111111-2222-3333-4444-555555555555",
  "landlordId": "07767213-612b-4f0b-987c-75e43e5ca152",
  "addressLine1": "Alice Apartments",
  "city": "Mombasa",
  "status": "AVAILABLE",
  "unitIds": [
    "2dbaaafc-3ede-4d06-8701-73aa8530a573"
  ]
}
```

> `landlordId` is the user's `publicId`, not the internal `Landlord.id`.

### Errors

| Status | When                             |
| ------ | -------------------------------- |
| `400`  | `addressLine1` or `city` missing |
| `403`  | Caller is not LANDLORD           |
| `409`  | `propertyId` already exists      |

---

## 5.2 Landlords

| Method | Path                      | Description  |
| ------ | ------------------------- | ------------ |
| GET    | `/landlords`              | List all     |
| GET    | `/landlords/{id}`         | Get by UUID  |
| GET    | `/landlords/email?email=` | Get by email |
| POST   | `/landlords`              | Create       |
| PUT    | `/landlords/{id}`         | Update       |
| DELETE | `/landlords/{id}`         | Delete       |

---

## 5.3 Units

| Method | Path                           | Description        |
| ------ | ------------------------------ | ------------------ |
| GET    | `/units`                       | List all           |
| GET    | `/units/{id}`                  | Get by UUID        |
| GET    | `/units/property/{propertyId}` | Units for property |
| GET    | `/units/status/{status}`       | Filter by status   |
| POST   | `/units`                       | Create             |
| PUT    | `/units/{id}`                  | Update             |
| PATCH  | `/units/{id}/status`           | Change status      |
| PATCH  | `/units/{id}/assign-tenant`    | Assign tenant      |
| PATCH  | `/units/{id}/vacate`           | Vacate             |
| DELETE | `/units/{id}`                  | Delete             |

### Create Unit

```http
POST /api/v1/units
Authorization: Bearer <landlord-token>
Content-Type: application/json
```

### Request

```json
{
  "propertyId": "f37c55b5-1057-459d-825c-3fac95dd71bc",
  "bedrooms": 2,
  "bathrooms": 1,
  "monthlyRent": 24000,
  "securityDeposit": 48000
}
```

`unitNumber` is optional. The backend auto-generates `A`, `B`, `C`, etc. per property.

### Response

```json
{
  "id": "2dbaaafc-3ede-4d06-8701-73aa8530a573",
  "unitNumber": "A",
  "propertyId": "f37c55b5-1057-459d-825c-3fac95dd71bc",
  "bedrooms": 2,
  "bathrooms": 1,
  "monthlyRent": 24000,
  "status": "AVAILABLE"
}
```

### Errors

| Status | When                                     |
| ------ | ---------------------------------------- |
| `400`  | `propertyId` missing                     |
| `404`  | Property not found                       |
| `409`  | `unitNumber` already exists for property |

---

# 6. Lease Service

Base path:

```text
/api/v1/leases
```

| Method | Path                                       | Roles    | Description               |
| ------ | ------------------------------------------ | -------- | ------------------------- |
| GET    | `/leases`                                  | Any      | List leases — auto-scoped |
| GET    | `/leases/{id}`                             | Any      | Get by UUID               |
| POST   | `/leases`                                  | TENANT   | Apply for lease           |
| PUT    | `/leases/{id}`                             | Any      | Update                    |
| POST   | `/leases/{id}/approve`                     | LANDLORD | Approve DRAFT → ACTIVE    |
| POST   | `/leases/{id}/terminate?terminationDate=X` | LANDLORD | Terminate ACTIVE          |
| POST   | `/leases/{id}/renew?newEndDate=X`          | LANDLORD | Extend end date           |
| DELETE | `/leases/{id}`                             | Any      | Cancel DRAFT              |

## Apply for a Lease

```http
POST /api/v1/leases
Authorization: Bearer <tenant-token>
Content-Type: application/json
```

### Request

```json
{
  "propertyId": "f37c55b5-1057-459d-825c-3fac95dd71bc",
  "tenantId": "c0d4c5dc-80a8-41d9-98d2-b7ec2ea3442a",
  "landlordId": "07767213-612b-4f0b-987c-75e43e5ca152",
  "startDate": "2026-10-01",
  "endDate": "2027-10-01",
  "rentAmount": 24000,
  "depositAmount": 48000
}
```

### Response

```json
{
  "id": "45c03bf9-7364-45b4-963f-a39794d43253",
  "propertyId": "f37c55b5-1057-459d-825c-3fac95dd71bc",
  "tenantId": "c0d4c5dc-80a8-41d9-98d2-b7ec2ea3442a",
  "landlordId": "07767213-612b-4f0b-987c-75e43e5ca152",
  "startDate": "2026-10-01",
  "endDate": "2027-10-01",
  "rentAmount": 24000.0,
  "depositAmount": 48000.0,
  "status": "DRAFT",
  "createdAt": "2026-10-03T12:00:00"
}
```

### Errors

| Status | When                                |
| ------ | ----------------------------------- |
| `400`  | `endDate` before `startDate`        |
| `404`  | Property not found                  |
| `409`  | Overlapping lease for same property |

Side effect:

```text
lease.lease.created
```

The notification engine fans out the event to the tenant and landlord.

## List Leases

```http
GET /api/v1/leases
Authorization: Bearer <token>
```

Automatic filtering:

* TENANT → `tenantId = caller.publicId`
* LANDLORD → `landlordId = caller.publicId`
* ADMIN → all leases

### Response

```json
{
  "leases": [
    {
      "id": "45c03bf9-7364-45b4-963f-a39794d43253",
      "propertyId": "f37c55b5-1057-459d-825c-3fac95dd71bc",
      "tenantId": "c0d4c5dc-80a8-41d9-98d2-b7ec2ea3442a",
      "landlordId": "07767213-612b-4f0b-987c-75e43e5ca152",
      "startDate": "2026-10-01",
      "endDate": "2027-10-01",
      "rentAmount": 24000.0,
      "status": "ACTIVE"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

## Approve Lease

```http
POST /api/v1/leases/{id}/approve
Authorization: Bearer <landlord-token>
```

Response:

`200 OK`

Lease status becomes `ACTIVE`.

### Errors

| Status | When                            |
| ------ | ------------------------------- |
| `400`  | Lease is not DRAFT              |
| `403`  | Caller is not landlord on lease |
| `404`  | Lease not found                 |

Side effects:

```text
lease.lease.approved
lease.lease.created
```

## Terminate Lease

```http
POST /api/v1/leases/{id}/terminate?terminationDate=2026-12-31
Authorization: Bearer <landlord-token>
```

Response:

`200 OK`

Lease status becomes `TERMINATED`.

### Errors

| Status | When                                  |
| ------ | ------------------------------------- |
| `400`  | Lease not ACTIVE or date out of range |
| `404`  | Lease not found                       |

Side effect:

```text
lease.lease.terminated
```

## Renew Lease

```http
POST /api/v1/leases/{id}/renew?newEndDate=2028-10-01
Authorization: Bearer <landlord-token>
```

Response:

`200 OK`

The lease's `endDate` is extended.

### Errors

| Status | When                                                        |
| ------ | ----------------------------------------------------------- |
| `400`  | Lease not ACTIVE or new end date is before current end date |
| `409`  | Renewal period overlaps another lease                       |

## Update Lease

```http
PUT /api/v1/leases/{id}
Authorization: Bearer <token>
Content-Type: application/json
```

All fields are optional.

```json
{
  "startDate": "2026-10-01",
  "endDate": "2027-10-01",
  "rentAmount": 25000,
  "depositAmount": 50000,
  "termsAndConditions": "Updated terms..."
}
```

Response:

`200 OK`

---

# 7. Payment Service — Invoices

Base path:

```text
/api/v1/invoices
```

| Method | Path                                  | Description                 |
| ------ | ------------------------------------- | --------------------------- |
| GET    | `/invoices`                           | List — paginated/filterable |
| GET    | `/invoices/{id}`                      | Get by UUID                 |
| GET    | `/invoices/number/{invoiceNumber}`    | Get by invoice number       |
| POST   | `/invoices`                           | Create                      |
| PATCH  | `/invoices/{id}`                      | Update                      |
| PATCH  | `/invoices/{id}/pay?amount=X`         | Apply payment               |
| PATCH  | `/invoices/{id}/late-fee?feeAmount=X` | Apply late fee              |
| PATCH  | `/invoices/{id}/void`                 | Void                        |
| POST   | `/invoices/mark-overdue`              | Bulk mark overdue           |

## Invoice Statuses

```text
pending   — no payment yet
partial   — some paid, balance outstanding
paid      — fully paid
overdue   — due date + grace period passed with balance
voided    — cancelled
```

## Create Invoice

```http
POST /api/v1/invoices
Authorization: Bearer <landlord-token>
Content-Type: application/json
```

### Request

```json
{
  "leaseId": "45c03bf9-7364-45b4-963f-a39794d43253",
  "invoiceNumber": "INV-202610-ABC123",
  "periodStart": "2026-10-01",
  "periodEnd": "2026-10-31",
  "dueDate": "2026-11-05",
  "totalAmount": 24000,
  "paidAmount": 0,
  "gracePeriodDays": 3
}
```

### Response

```json
{
  "id": "37e9d919-9a71-4482-a90c-c41a98ba7021",
  "leaseId": "45c03bf9-7364-45b4-963f-a39794d43253",
  "invoiceNumber": "INV-202610-ABC123",
  "periodStart": "2026-10-01",
  "periodEnd": "2026-10-31",
  "dueDate": "2026-11-05",
  "totalAmount": 24000,
  "paidAmount": 0,
  "status": "pending",
  "gracePeriodDays": 3
}
```

### Errors

| Status | When                                |
| ------ | ----------------------------------- |
| `400`  | Missing field or `totalAmount <= 0` |
| `404`  | `leaseId` not found                 |
| `409`  | `invoiceNumber` already exists      |

Side effect:

```text
invoice.invoice.created
```

Notification recipients are resolved through the Lease Service.

## List Invoices

```http
GET /api/v1/invoices?leaseId=45c03bf9-...&status=partial&page=0&size=20
Authorization: Bearer <token>
```

All query parameters are optional.

Response:

`Spring Page<InvoiceResponseDto>`

## Apply Payment

```http
PATCH /api/v1/invoices/{id}/pay?amount=10000
Authorization: Bearer <tenant-token>
```

### Response

```json
{
  "id": "37e9d919-9a71-4482-a90c-c41a98ba7021",
  "totalAmount": 40000,
  "paidAmount": 30000,
  "status": "partial"
}
```

Send the remaining balance to transition the invoice to `paid`.

### Errors

| Status | When                               |
| ------ | ---------------------------------- |
| `400`  | Amount exceeds outstanding balance |
| `404`  | Invoice not found                  |
| `409`  | Invoice already paid or voided     |

## Apply Late Fee

```http
PATCH /api/v1/invoices/{id}/late-fee?feeAmount=500
Authorization: Bearer <landlord-token>
```

Response:

`204 No Content`

### Errors

| Status | When                                               |
| ------ | -------------------------------------------------- |
| `400`  | Late fee already applied or invoice is paid/voided |

## Void Invoice

```http
PATCH /api/v1/invoices/{id}/void
Authorization: Bearer <landlord-token>
```

Response:

`204 No Content`

### Errors

| Status | When                 |
| ------ | -------------------- |
| `400`  | Invoice already paid |

## Bulk Mark Overdue

```http
POST /api/v1/invoices/mark-overdue
Authorization: Bearer <admin-token>
```

Response:

`204 No Content`

The operation also runs automatically every day at `01:00`.

---

# 8. Payment Service — Invoice Line Items

Base path:

```text
/api/v1/invoice-line-items
```

| Method | Path                                               | Description      |
| ------ | -------------------------------------------------- | ---------------- |
| POST   | `/invoice-line-items`                              | Create line item |
| GET    | `/invoice-line-items/{id}`                         | Get by UUID      |
| GET    | `/invoice-line-items/by-invoice/{invoiceId}`       | All items        |
| GET    | `/invoice-line-items/by-invoice-paged/{invoiceId}` | Paginated        |
| PATCH  | `/invoice-line-items/{id}`                         | Update           |
| DELETE | `/invoice-line-items/{id}`                         | Delete           |
| DELETE | `/invoice-line-items/by-invoice/{invoiceId}`       | Delete all       |

## Create Line Item

```http
POST /api/v1/invoice-line-items
Authorization: Bearer <landlord-token>
Content-Type: application/json
```

### Request

```json
{
  "invoiceId": "37e9d919-9a71-4482-a90c-c41a98ba7021",
  "description": "Rent",
  "category": "rent",
  "quantity": 1,
  "unitPrice": 22000,
  "taxRate": 0
}
```

Valid categories:

```text
rent
utility
fee
deposit
```

### Response

```json
{
  "id": "e8b1c4d2-...",
  "invoiceId": "37e9d919-9a71-4482-a90c-c41a98ba7021",
  "description": "Rent",
  "category": "rent",
  "quantity": 1,
  "unitPrice": 22000,
  "total": 22000,
  "taxRate": 0
}
```

`total = (unitPrice × quantity) + tax`

### Errors

| Status | When                                              |
| ------ | ------------------------------------------------- |
| `400`  | Missing field, `unitPrice <= 0`, invalid category |
| `404`  | Invoice not found                                 |

---

# 9. Payment Service — Security Deposits

Base path:

```text
/api/v1/security-deposits
```

| Method | Path                                              | Description      |
| ------ | ------------------------------------------------- | ---------------- |
| POST   | `/security-deposits`                              | Record deposit   |
| GET    | `/security-deposits/{id}`                         | Get by UUID      |
| GET    | `/security-deposits/by-lease/{leaseId}`           | Get for lease    |
| GET    | `/security-deposits`                              | List — paginated |
| PATCH  | `/security-deposits/{id}`                         | Update           |
| PATCH  | `/security-deposits/{id}/deduction`               | Add deduction    |
| PATCH  | `/security-deposits/{id}/return`                  | Mark returned    |
| PATCH  | `/security-deposits/{id}/interest?interestRate=X` | Accrue interest  |
| DELETE | `/security-deposits/{id}`                         | Delete           |

## Deposit Statuses

```text
ACTIVE
PARTIALLY_DEDUCTED
RETURNED
```

## Record Deposit

```http
POST /api/v1/security-deposits
Authorization: Bearer <landlord-token>
Content-Type: application/json
```

### Request

```json
{
  "leaseId": "45c03bf9-7364-45b4-963f-a39794d43253",
  "totalDeposit": 48000
}
```

### Response

```json
{
  "id": "d8a7b6c5-...",
  "leaseId": "45c03bf9-7364-45b4-963f-a39794d43253",
  "totalDeposit": 48000,
  "currentBalance": 48000,
  "deductionTotal": null,
  "returnedDate": null,
  "status": "ACTIVE"
}
```

### Errors

| Status | When                             |
| ------ | -------------------------------- |
| `400`  | `totalDeposit <= 0`              |
| `404`  | `leaseId` not found              |
| `409`  | Deposit already exists for lease |

## Apply Deduction

```http
PATCH /api/v1/security-deposits/{id}/deduction
Authorization: Bearer <landlord-token>
Content-Type: application/json
```

### Request

```json
{
  "amount": 2000,
  "description": "Broken window"
}
```

### Response

```json
{
  "currentBalance": 46000,
  "deductionTotal": 2000,
  "status": "PARTIALLY_DEDUCTED"
}
```

### Errors

| Status | When                           |
| ------ | ------------------------------ |
| `400`  | Amount exceeds current balance |
| `409`  | Deposit already returned       |

## Return Deposit

```http
PATCH /api/v1/security-deposits/{id}/return
Authorization: Bearer <landlord-token>
Content-Type: application/json
```

### Request

```json
{
  "returnAmount": 46000,
  "returnedDate": "2026-10-03"
}
```

### Response

`200 OK`

Deposit status becomes `RETURNED`.

### Errors

| Status | When                                  |
| ------ | ------------------------------------- |
| `400`  | Return amount exceeds current balance |
| `409`  | Deposit already returned              |

---

# 10. Payment Service — Payments

Base path:

```text
/api/v1/payments
```

| Method | Path                     | Description           |
| ------ | ------------------------ | --------------------- |
| GET    | `/payments`              | List — paginated      |
| GET    | `/payments/{id}`         | Get by UUID           |
| POST   | `/payments`              | Create payment record |
| PATCH  | `/payments/{id}`         | Update                |
| PATCH  | `/payments/{id}/process` | Mark processing       |
| PATCH  | `/payments/{id}/refund`  | Refund                |
| PATCH  | `/payments/{id}/apply`   | Apply to invoice      |
| DELETE | `/payments/{id}`         | Delete                |

> These endpoints are administrative. In the normal flow, payments are recorded automatically through the M-Pesa callback. The tenant UI does not call `POST /payments` directly.

---

# 11. Payment Service — Payment Splits

Base path:

```text
/api/v1/payment-splits
```

| Method | Path                                           | Description                 |
| ------ | ---------------------------------------------- | --------------------------- |
| POST   | `/payment-splits`                              | Allocate payment to invoice |
| GET    | `/payment-splits/{id}`                         | Get by UUID                 |
| GET    | `/payment-splits/by-payment/{paymentId}`       | Splits for payment          |
| GET    | `/payment-splits/by-payment-paged/{paymentId}` | Paginated                   |
| GET    | `/payment-splits/by-invoice/{invoiceId}`       | Splits applied to invoice   |
| PATCH  | `/payment-splits/{id}`                         | Update                      |
| DELETE | `/payment-splits/{id}`                         | Delete                      |

> No `GET /payment-splits` list-all endpoint exists. The frontend aggregates splits per invoice.

## Create Split

```http
POST /api/v1/payment-splits
Authorization: Bearer <landlord-token>
Content-Type: application/json
```

### Request

```json
{
  "paymentId": "p1a2b3c4-...",
  "invoiceId": "37e9d919-9a71-4482-a90c-c41a98ba7021",
  "allocatedAmount": 24000
}
```

### Response

```json
{
  "id": "s9d8f7e6-...",
  "paymentId": "p1a2b3c4-...",
  "invoiceId": "37e9d919-9a71-4482-a90c-c41a98ba7021",
  "allocatedAmount": 24000,
  "createdAt": "2026-10-03T12:34:56"
}
```

---

# 12. Payment Service — Payment Methods & Accounts

## Payment Methods

Base path:

```text
/api/v1/payment-methods
```

| Method | Path                                    | Description        |
| ------ | --------------------------------------- | ------------------ |
| POST   | `/payment-methods`                      | Register method    |
| GET    | `/payment-methods/{id}`                 | Get by UUID        |
| GET    | `/payment-methods/by-tenant/{tenantId}` | Methods for tenant |
| PATCH  | `/payment-methods/{id}`                 | Update             |
| PATCH  | `/payment-methods/{id}/set-default`     | Set as default     |
| PATCH  | `/payment-methods/{id}/toggle-active`   | Enable/disable     |
| DELETE | `/payment-methods/{id}`                 | Delete             |

## Payment Accounts

Base path:

```text
/api/v1/payment-accounts
```

| Method | Path                                                | Description         |
| ------ | --------------------------------------------------- | ------------------- |
| POST   | `/payment-accounts`                                 | Create              |
| GET    | `/payment-accounts/{id}`                            | Get by UUID         |
| GET    | `/payment-accounts/by-entity?entityId=&entityType=` | Accounts for entity |
| GET    | `/payment-accounts`                                 | List                |
| PATCH  | `/payment-accounts/{id}`                            | Update              |
| POST   | `/payment-accounts/{id}/adjust`                     | Manual adjustment   |
| POST   | `/payment-accounts/{id}/transfer`                   | Transfer            |
| DELETE | `/payment-accounts/{id}`                            | Delete              |

---

# 13. Payment Service — Audit Logs

Base path:

```text
/api/v1/audit-logs
```

| Method | Path                              | Description      |
| ------ | --------------------------------- | ---------------- |
| GET    | `/audit-logs/account/{accountId}` | Logs for account |
| GET    | `/audit-logs/search?q=&from=&to=` | Search           |

Audit logs are read-only and are automatically populated by write operations.

---

# 14. M-Pesa Service

Base path:

```text
/api/v1/mpesa
```

## 14.1 Endpoints

| Method | Path                                    | Auth   | Description            |
| ------ | --------------------------------------- | ------ | ---------------------- |
| POST   | `/mpesa/stk-push`                       | TENANT | Initiate STK push      |
| POST   | `/mpesa/callback`                       | Public | Safaricom callback     |
| POST   | `/mpesa/query-status`                   | Any    | Query Safaricom        |
| GET    | `/mpesa/transactions/{id}`              | Any    | Get transaction        |
| GET    | `/mpesa/transactions/tenant/{tenantId}` | Any    | Transactions by tenant |
| GET    | `/mpesa/transactions/lease/{leaseId}`   | Any    | Transactions by lease  |

## 14.2 Initiate STK Push

```http
POST /api/v1/mpesa/stk-push
Authorization: Bearer <tenant-token>
Content-Type: application/json
```

### Request

```json
{
  "phone": "254708374149",
  "amount": 24000,
  "leaseId": "45c03bf9-7364-45b4-963f-a39794d43253",
  "tenantId": "c0d4c5dc-80a8-41d9-98d2-b7ec2ea3442a",
  "accountReference": "INV-202610-ABC123",
  "transactionDesc": "Rent payment"
}
```

### Validation

* `phone` must match `^254[0-9]{9}$`
* Phone must contain 12 digits and no `+`
* `amount` must be a positive integer
* `accountReference` is required
* `transactionDesc` is required
* `tenantId` is required
* `leaseId` is required

### Response

```json
{
  "merchantRequestId": "4e7d-425c-aa3e-9b62f1dcf8a21114371",
  "checkoutRequestId": "ws_CO_011020261617137757380426",
  "responseCode": "0",
  "responseDescription": "Success. Request accepted for processing",
  "customerMessage": "Success. Request accepted for processing"
}
```

### Errors

| Status | When                                  |
| ------ | ------------------------------------- |
| `400`  | Invalid phone format or missing field |
| `500`  | Safaricom rejected request            |

Side effect:

A `Transaction` row is created with:

```text
status = PENDING
```

## 14.3 Callback

Safaricom sends the callback to:

```http
POST /api/v1/mpesa/callback
Content-Type: application/json
```

No JWT is required.

### Success Payload

```json
{
  "Body": {
    "stkCallback": {
      "MerchantRequestID": "4e7d-...",
      "CheckoutRequestID": "ws_CO_011020261617137757380426",
      "ResultCode": 0,
      "ResultDesc": "The service request is processed successfully.",
      "CallbackMetadata": {
        "Item": [
          {
            "Name": "Amount",
            "Value": 24000
          },
          {
            "Name": "MpesaReceiptNumber",
            "Value": "QK1234567890"
          },
          {
            "Name": "PhoneNumber",
            "Value": 254708374149
          }
        ]
      }
    }
  }
}
```

### Failure Payload

```json
{
  "Body": {
    "stkCallback": {
      "MerchantRequestID": "4e7d-...",
      "CheckoutRequestID": "ws_CO_...",
      "ResultCode": 1,
      "ResultDesc": "The balance is insufficient for the transaction.",
      "CallbackMetadata": null
    }
  }
}
```

### Callback Response

```json
{
  "ResultCode": 0,
  "ResultDescription": "Success"
}
```

## 14.4 Callback Behaviour

The callback always returns HTTP `200`, including internal processing errors, to prevent Safaricom retry storms.

Failed callbacks are logged to `notification_logs` for reconciliation.

Callback processing should complete in less than two seconds because Safaricom retries on timeout.

### Successful Callback

When `ResultCode == 0`:

1. Transaction status becomes `SUCCESS`.
2. `mpesaReceiptNumber` is stored.
3. `mpesa.events.stk.result` is published.

### Failed Callback

When `ResultCode != 0`:

1. Transaction status becomes `FAILED`.
2. `resultDescription` stores Safaricom's error message.
3. `mpesa.events.stk.result` is still published with `status=FAILED`.

## 14.5 Test the Flow

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"bob@test.com","password":"Passw0rd!"}' \
  | python -c "import sys,json; print(json.load(sys.stdin)['accessToken'])")

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
    "transactionDesc":"Test"
  }'
```

Use `254708374149` as the sandbox test MSISDN.

## 14.6 Common STK Push Errors

| Error                                | Cause                     | Fix                                |
| ------------------------------------ | ------------------------- | ---------------------------------- |
| `400 Bad Request [no body] on OAuth` | Empty consumer key/secret | Check `.env DARAJA_*` variables    |
| `Receiver not whitelisted`           | Phone not approved        | Use `254708374149`                 |
| `Invalid Access Token`               | Sandbox OAuth expired     | Retry                              |
| `9999 Error occurred`                | Sandbox instability       | Retry 3–5 times or wait 10 minutes |
| Callback never arrives               | ngrok down or stale URL   | Restart ngrok and update `.env`    |

---

# 15. Maintenance Service

Base path:

```text
/api/v1/tickets
```

| Method | Path                             | Roles           | Description          |
| ------ | -------------------------------- | --------------- | -------------------- |
| GET    | `/tickets`                       | Any             | List — auto-scoped   |
| GET    | `/tickets/{id}`                  | Any             | Get by UUID          |
| GET    | `/tickets/unit/{unitId}`         | Any             | Tickets for unit     |
| GET    | `/tickets/tenant/{tenantId}`     | Any             | Tickets for tenant   |
| GET    | `/tickets/landlord/{landlordId}` | Any             | Tickets for landlord |
| GET    | `/tickets/status/{status}`       | Any             | Filter by status     |
| POST   | `/tickets`                       | TENANT/LANDLORD | Create               |
| PUT    | `/tickets/{id}`                  | Any             | Update               |
| PATCH  | `/tickets/{id}/status?status=X`  | LANDLORD        | Change status        |
| DELETE | `/tickets/{id}`                  | Any             | Delete               |

## Ticket Statuses

```text
OPEN
IN_PROGRESS
RESOLVED
CLOSED
```

## Ticket Priorities

```text
LOW
MEDIUM
HIGH
URGENT
```

## Create Ticket — Tenant

```http
POST /api/v1/tickets
Authorization: Bearer <tenant-token>
Content-Type: application/json
```

### Request

```json
{
  "unitId": "2dbaaafc-3ede-4d06-8701-73aa8530a573",
  "title": "AC not working",
  "description": "Living room AC blows warm air",
  "priority": "HIGH",
  "landlordId": "07767213-612b-4f0b-987c-75e43e5ca152"
}
```

### Response

```json
{
  "id": "c7db99eb-2879-4ed7-a1f7-20e3cb6eca9c",
  "unitId": "2dbaaafc-3ede-4d06-8701-73aa8530a573",
  "tenantId": "c0d4c5dc-80a8-41d9-98d2-b7ec2ea3442a",
  "landlordId": "07767213-612b-4f0b-987c-75e43e5ca152",
  "title": "AC not working",
  "priority": "HIGH",
  "status": "OPEN",
  "createdAt": "2026-10-03T12:00:00"
}
```

### Role-Based Logic

TENANT caller:

```text
tenantId = caller.publicId
landlordId = required from request
```

LANDLORD caller:

```text
landlordId = caller.publicId
tenantId = required from request
```

### Errors

| Status | When                                     |
| ------ | ---------------------------------------- |
| `400`  | Missing title                            |
| `400`  | Missing landlordId when called by tenant |
| `400`  | Missing tenantId when called by landlord |
| `403`  | Caller is not TENANT or LANDLORD         |
| `404`  | Unit not found                           |

Side effect:

```text
maintenance.ticket.created
```

## Resolve Ticket

```http
PATCH /api/v1/tickets/{id}/status?status=RESOLVED
Authorization: Bearer <landlord-token>
```

Response:

`200 OK`

Ticket status becomes `RESOLVED`.

Side effect:

```text
maintenance.ticket.resolved
```

The event is published when status becomes `RESOLVED` or `CLOSED`.

## List Tickets

```http
GET /api/v1/tickets
Authorization: Bearer <token>
```

Automatic filtering:

* TENANT → `findByTenantId(callerPublicId)`
* LANDLORD → `findByLandlordId(callerPublicId)`
* ADMIN → all

---

# 16. Notification Engine

Base path:

```text
/api/v1/notifications
```

| Method | Path                        | Description                 |
| ------ | --------------------------- | --------------------------- |
| GET    | `/notifications/logs`       | List notification logs      |
| GET    | `/notifications/logs/{id}`  | Get log entry               |
| POST   | `/notifications/retry/{id}` | Mark failed entry for retry |

## List Notification Logs

```http
GET /api/v1/notifications/logs
Authorization: Bearer <token>
```

### Response

```json
[
  {
    "id": "6abb9f3e5a218c30132153f1",
    "channel": "IN_APP",
    "status": "SENT",
    "eventType": "MAINTENANCE_CREATED",
    "payload": "{\"ticketId\":\"...\",\"title\":\"water pipe leakage\"}",
    "attempts": 0,
    "errorMessage": null,
    "createdAt": "2026-10-03T12:34:56.789",
    "updatedAt": "2026-10-03T12:34:56.789"
  }
]
```

## Channels

```text
IN_APP
SMS
WHATSAPP
EMAIL
```

> `EMAIL` is defined but not yet wired.

## Notification Statuses

```text
PENDING
SENT
FAILED
```

## WebSocket

Endpoint:

```text
ws://localhost:8080/ws
```

Protocol:

```text
STOMP over SockJS
```

Subscription:

```text
/user/queue/notifications
```

Payload:

```text
Plain string containing the notification summary.
```

### JavaScript Example

```javascript
const socket = new SockJS('http://localhost:8080/ws');
const stomp = Stomp.over(socket);

stomp.connect(
  { Authorization: `Bearer ${token}` },
  () => {
    stomp.subscribe('/user/queue/notifications', (msg) => {
      console.log('Notification:', msg.body);
    });
  }
);
```

> **Note:** JWT authentication on WebSocket is pending implementation.

---

# 17. Gateway Route Table

Routes are defined in:

```text
gateway-service/src/main/java/com/rentflow/gateway/GatewayApplication.java
```

| Route ID           | Path Prefixes                                                                                                                                                                                                                              | Target Service              |
| ------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | --------------------------- |
| `auth`             | `/api/v1/auth/**`                                                                                                                                                                                                                          | AUTH-SERVICE                |
| `users`            | `/api/v1/users/**`                                                                                                                                                                                                                         | AUTH-SERVICE                |
| `property-service` | `/api/v1/properties/**`, `/api/v1/landlords/**`, `/api/v1/units/**`                                                                                                                                                                        | PROPERTY-MANAGEMENT-SERVICE |
| `lease`            | `/api/v1/leases/**`                                                                                                                                                                                                                        | LEASE-SERVICE               |
| `payment`          | `/api/v1/payments/**`                                                                                                                                                                                                                      | PAYMENT-SERVICE             |
| `invoices`         | `/api/v1/invoices/**`, `/api/v1/invoice-line-items/**`, `/api/v1/security-deposits/**`, `/api/v1/deposit-deductions/**`, `/api/v1/payment-splits/**`, `/api/v1/payment-methods/**`, `/api/v1/payment-accounts/**`, `/api/v1/audit-logs/**` | PAYMENT-SERVICE             |
| `mpesa`            | `/api/v1/mpesa/**`                                                                                                                                                                                                                         | MPESA-SERVICE               |
| `maintenance`      | `/api/v1/maintenance/**`, `/api/v1/tickets/**`                                                                                                                                                                                             | MAINTENANCE-TICKET-SERVICE  |
| `notification`     | `/api/v1/notifications/**`                                                                                                                                                                                                                 | NOTIFICATION-ENGINE         |

## Public Paths

The following paths do not require JWT authentication:

```text
/api/v1/auth/login
/api/v1/auth/register
/api/v1/auth/refresh
/api/v1/auth/callback
/api/v1/mpesa/callback
/actuator/**
```

---

# 18. Kafka Event Reference

Every event shares the following envelope:

```json
{
  "eventType": "lease.lease.approved",
  "correlationId": "uuid",
  "tenantId": "uuid",
  "landlordId": "uuid"
}
```

Additional domain-specific fields may be included.

| Topic                          | Producer    | Event Type                     | Recipients        |
| ------------------------------ | ----------- | ------------------------------ | ----------------- |
| `auth.user.events`             | auth        | `user.registered`              | —                 |
| `auth.user.events`             | auth        | `user.login`                   | —                 |
| `lease.lease.created`          | lease       | `lease.lease.created`          | Tenant + landlord |
| `lease.lease.approved`         | lease       | `lease.lease.approved`         | Tenant + landlord |
| `lease.lease.terminated`       | lease       | `lease.lease.terminated`       | Tenant + landlord |
| `property.property.registered` | property    | `property.property.registered` | Landlord          |
| `invoice.invoice.created`      | payment     | `invoice.invoice.created`      | Tenant + landlord |
| `mpesa.events.stk.result`      | mpesa       | `mpesa.events.stk.result`      | Tenant            |
| `maintenance.ticket.created`   | maintenance | `maintenance.ticket.created`   | Tenant + landlord |
| `maintenance.ticket.resolved`  | maintenance | `maintenance.ticket.resolved`  | Tenant + landlord |

The Notification Engine consumes these events and dispatches notifications through:

```text
In-App
SMS
WhatsApp
```

These channels are mocked in development.

---

# 19. Error Code Reference

## By Endpoint

| Endpoint                                  | `400` if                   | `404` if           | `409` if             |
| ----------------------------------------- | -------------------------- | ------------------ | -------------------- |
| `POST /auth/login`                        | Missing email/password     | —                  | —                    |
| `POST /auth/register`                     | Weak password              | —                  | Email taken          |
| `POST /properties`                        | Address/city missing       | —                  | Property ID taken    |
| `POST /units`                             | Property ID missing        | Property not found | Unit number taken    |
| `POST /leases`                            | End date before start date | Property not found | Overlapping lease    |
| `POST /leases/{id}/approve`               | Not DRAFT                  | Lease not found    | —                    |
| `POST /invoices`                          | Total amount ≤ 0           | Lease not found    | Invoice number taken |
| `PATCH /invoices/{id}/pay`                | Amount > outstanding       | Invoice not found  | Already paid/voided  |
| `POST /invoice-line-items`                | Unit price ≤ 0             | Invoice not found  | —                    |
| `POST /security-deposits`                 | Total deposit ≤ 0          | Lease not found    | Deposit exists       |
| `PATCH /security-deposits/{id}/deduction` | Amount > balance           | Deposit not found  | Already returned     |
| `POST /mpesa/stk-push`                    | Phone format wrong         | —                  | —                    |
| `POST /tickets`                           | Title missing              | Unit not found     | —                    |

## HTTP Status Semantics

| Code  | When to expect                                     |
| ----- | -------------------------------------------------- |
| `200` | Successful GET, PATCH, or POST returning a body    |
| `201` | Successful POST creating a resource                |
| `204` | Successful DELETE or body-less PATCH               |
| `400` | Client sent invalid data                           |
| `401` | Token missing, expired, or invalid                 |
| `403` | Valid token but wrong role                         |
| `404` | Resource does not exist or route is not registered |
| `409` | Business-rule conflict                             |
| `500` | Server error                                       |
| `503` | Target service is not registered in Eureka         |

For `500`, check:

```text
docker logs rentflow-<service>
```

---

# 20. Versioning & Rate Limits

## API Versioning

All endpoints use URL-level versioning:

```text
/api/v1/
```

Breaking changes such as:

* Removed fields
* Changed field types
* New required fields

require a new API prefix:

```text
/api/v2/
```

Non-breaking changes such as new endpoints or optional fields remain under the existing version.

Deprecated endpoints use a `Sunset` response header for 90 days before removal.

No `Accept-Version` header is used.

## Rate Limits

Rate limits are **not enforced in development**.

Planned production limits:

| Endpoint                      |         Limit | Window           |
| ----------------------------- | ------------: | ---------------- |
| `POST /auth/login`            |     10 per IP | 1 minute         |
| `POST /auth/register`         |      5 per IP | 1 minute         |
| `POST /mpesa/stk-push`        |  5 per tenant | 1 minute         |
| Other authenticated endpoints | 1000 per user | 1 minute         |
| `POST /mpesa/callback`        |     Unlimited | Safaricom origin |

When enforced, responses include:

```text
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 847
X-RateLimit-Reset: 1790000060
```

Exceeded limits return:

```json
{
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Try again in 42 seconds."
}
```

## Idempotency

The Payment Service supports idempotency keys for:

```http
POST /api/v1/payments
```

Header:

```text
Idempotency-Key: <uuid>
```

Duplicate requests within 24 hours return the original response.

The feature is currently enabled but unused by the frontend.

---

# 21. Complete Test Sequence

The following script tests the primary RentFlow flow end-to-end.

```bash
#!/bin/bash

set -e

# ============================================
# 1. Login as landlord
# ============================================

ALICE=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@test.com","password":"Passw0rd!"}' \
  | python -c "import sys,json; print(json.load(sys.stdin)['accessToken'])")

echo "Alice token length: ${#ALICE}"

# ============================================
# 2. Create property + first unit
# ============================================

curl -s -X POST http://localhost:8080/api/v1/properties \
  -H "Authorization: Bearer $ALICE" \
  -H "X-Tenant-ID: default" \
  -H "Content-Type: application/json" \
  -d '{
    "propertyId":"11111111-2222-3333-4444-555555555555",
    "addressLine1":"Test Apartments",
    "city":"Nairobi"
  }' | python -m json.tool

# ============================================
# 3. Login as tenant
# ============================================

BOB=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"bob@test.com","password":"Passw0rd!"}' \
  | python -c "import sys,json; print(json.load(sys.stdin)['accessToken'])")

echo "Bob token length: ${#BOB}"

# ============================================
# 4. Apply for lease
# ============================================

curl -s -X POST http://localhost:8080/api/v1/leases \
  -H "Authorization: Bearer $BOB" \
  -H "X-Tenant-ID: default" \
  -H "Content-Type: application/json" \
  -d '{
    "propertyId":"f37c55b5-1057-459d-825c-3fac95dd71bc",
    "tenantId":"c0d4c5dc-80a8-41d9-98d2-b7ec2ea3442a",
    "landlordId":"07767213-612b-4f0b-987c-75e43e5ca152",
    "startDate":"2026-10-01",
    "endDate":"2027-10-01",
    "rentAmount":24000,
    "depositAmount":48000
  }' | python -m json.tool

# ============================================
# 5. Landlord approves lease
# ============================================

LEASE_ID=$(curl -s http://localhost:8080/api/v1/leases \
  -H "Authorization: Bearer $ALICE" \
  -H "X-Tenant-ID: default" \
  | python -c "import sys,json; d=json.load(sys.stdin); print(next(iter(d['leases']))['id'])")

echo "Lease ID: $LEASE_ID"

curl -s -X POST "http://localhost:8080/api/v1/leases/$LEASE_ID/approve" \
  -H "Authorization: Bearer $ALICE" \
  -H "X-Tenant-ID: default" \
  | python -m json.tool

# ============================================
# 6. Landlord creates invoice
# ============================================

INV_NUM="INV-TEST-$RANDOM"

curl -s -X POST http://localhost:8080/api/v1/invoices \
  -H "Authorization: Bearer $ALICE" \
  -H "X-Tenant-ID: default" \
  -H "Content-Type: application/json" \
  -d "{
    \"leaseId\":\"$LEASE_ID\",
    \"invoiceNumber\":\"$INV_NUM\",
    \"periodStart\":\"2026-10-01\",
    \"periodEnd\":\"2026-10-31\",
    \"dueDate\":\"2026-11-05\",
    \"totalAmount\":24000,
    \"paidAmount\":0,
    \"gracePeriodDays\":3
  }" | python -m json.tool

# ============================================
# 7. Tenant pays via M-Pesa
# ============================================

curl -s -X POST http://localhost:8080/api/v1/mpesa/stk-push \
  -H "Authorization: Bearer $BOB" \
  -H "X-Tenant-ID: default" \
  -H "Content-Type: application/json" \
  -d "{
    \"phone\":\"254708374149\",
    \"amount\":1,
    \"leaseId\":\"$LEASE_ID\",
    \"tenantId\":\"c0d4c5dc-80a8-41d9-98d2-b7ec2ea3442a\",
    \"accountReference\":\"TEST\",
    \"transactionDesc\":\"Test\"
  }" | python -m json.tool

# ============================================
# 8. Tenant creates maintenance ticket
# ============================================

curl -s -X POST http://localhost:8080/api/v1/tickets \
  -H "Authorization: Bearer $BOB" \
  -H "X-Tenant-ID: default" \
  -H "Content-Type: application/json" \
  -d '{
    "unitId":"2dbaaafc-3ede-4d06-8701-73aa8530a573",
    "title":"Test issue",
    "priority":"MEDIUM",
    "landlordId":"07767213-612b-4f0b-987c-75e43e5ca152"
  }' | python -m json.tool

# ============================================
# 9. Check notifications
# ============================================

echo ""
echo "=== Recent notifications ==="

docker logs --tail 50 rentflow-notification 2>&1 | grep "Routed event"
```

Expected after step 9:

```text
Routed event: type=...
```

Several events should show fan-out to the tenant and landlord across the configured development channels.

---

# 22. Appendix — Response Objects

## UserProfile

```json
{
  "id": "uuid",
  "name": "Bob Tenant",
  "email": "bob@test.com",
  "role": "TENANT",
  "phone": "+254757380426",
  "avatar": null
}
```

## Property

```json
{
  "id": "uuid",
  "propertyId": "uuid",
  "landlordId": "uuid (user publicId)",
  "addressLine1": "string",
  "addressLine2": "string|null",
  "city": "string",
  "state": "string|null",
  "zipCode": "string|null",
  "country": "string|null",
  "status": "AVAILABLE|OCCUPIED|MAINTENANCE",
  "unitIds": [
    "uuid"
  ]
}
```

## Unit

```json
{
  "id": "uuid",
  "unitNumber": "string",
  "propertyId": "uuid",
  "bedrooms": 2,
  "bathrooms": 1,
  "squareFeet": null,
  "monthlyRent": 24000,
  "securityDeposit": null,
  "status": "AVAILABLE|OCCUPIED|MAINTENANCE",
  "currentTenantId": "uuid|null"
}
```

## Lease

```json
{
  "id": "uuid",
  "propertyId": "uuid",
  "tenantId": "uuid",
  "landlordId": "uuid",
  "startDate": "yyyy-MM-dd",
  "endDate": "yyyy-MM-dd",
  "rentAmount": 24000.0,
  "depositAmount": 48000.0,
  "status": "DRAFT|ACTIVE|TERMINATED|EXPIRED|CANCELLED",
  "termsAndConditions": "string|null",
  "createdAt": "ISO-8601",
  "updatedAt": "ISO-8601"
}
```

## Invoice

```json
{
  "id": "uuid",
  "leaseId": "uuid",
  "invoiceNumber": "string",
  "periodStart": "yyyy-MM-dd",
  "periodEnd": "yyyy-MM-dd",
  "dueDate": "yyyy-MM-dd",
  "totalAmount": 24000,
  "paidAmount": 0,
  "status": "pending|partial|paid|overdue|voided",
  "lateFeeApplied": false,
  "gracePeriodDays": 3,
  "metadata": null,
  "createdAt": "ISO-8601",
  "updatedAt": "ISO-8601"
}
```

## InvoiceLineItem

```json
{
  "id": "uuid",
  "invoiceId": "uuid",
  "description": "Rent",
  "category": "rent|utility|fee|deposit",
  "quantity": 1,
  "unitPrice": 22000,
  "total": 22000,
  "taxRate": 0,
  "createdAt": "ISO-8601",
  "updatedAt": "ISO-8601"
}
```

## SecurityDeposit

```json
{
  "id": "uuid",
  "leaseId": "uuid",
  "totalDeposit": 48000,
  "currentBalance": 46000,
  "interestAccrued": 0,
  "deductionTotal": 2000,
  "returnedDate": "yyyy-MM-dd|null",
  "returnAmount": 46000,
  "status": "ACTIVE|PARTIALLY_DEDUCTED|RETURNED"
}
```

## Ticket

```json
{
  "id": "uuid",
  "unitId": "uuid",
  "tenantId": "uuid",
  "landlordId": "uuid",
  "title": "string",
  "description": "string",
  "priority": "LOW|MEDIUM|HIGH|URGENT",
  "status": "OPEN|IN_PROGRESS|RESOLVED|CLOSED",
  "createdAt": "ISO-8601",
  "updatedAt": "ISO-8601"
}
```

## MpesaTransaction

```json
{
  "id": "uuid",
  "checkoutRequestId": "ws_CO_...",
  "merchantRequestId": "string",
  "phoneNumber": "254708374149",
  "amount": 24000.0,
  "mpesaReceiptNumber": "QK...",
  "resultCode": 0,
  "resultDescription": "success",
  "status": "PENDING|SUCCESS|FAILED",
  "accountReference": "INV-202610-ABC123",
  "tenantId": "uuid",
  "leaseId": "uuid",
  "invoiceId": "uuid",
  "createdAt": "ISO-8601",
  "updatedAt": "ISO-8601"
}
```

## NotificationLog

```json
{
  "id": "string (Mongo ObjectId)",
  "channel": "IN_APP|SMS|WHATSAPP|EMAIL",
  "status": "PENDING|SENT|FAILED",
  "eventType": "string",
  "payload": "string (JSON)",
  "attempts": 0,
  "errorMessage": "string|null",
  "createdAt": "ISO-8601",
  "updatedAt": "ISO-8601"
}
```

---

**End of RentFlow API Reference**
