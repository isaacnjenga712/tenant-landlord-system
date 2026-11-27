# Pilot Evidence — Day 3 Cross-Tenant Probes

## Probe 1: Cross-tenant GET

**Request:**
- `X-Org-Id: 00000000-0000-0000-0000-000000000002` (org 2)
- `Authorization: Bearer <org-1-token>`
- `X-User-PublicId: 739ed6b8-5bb5-476b-921b-78b166a67b73` (org 1 recipient)

**Response:**
- `200 OK`
- `{"items":[],"total":0,"page":0,"size":5,"hasMore":false}`

**Verdict:** PASS — `TenantFilter` scoped the query to org 2. No org-1 data leaked.

## Probe 2: Cross-tenant WRITE

**Request:**
- `X-Org-Id: 00000000-0000-0000-0000-000000000002` (org 2)
- `Authorization: Bearer <org-1-token>`
- `PATCH /api/v1/notifications/inbox/{org-1-notification-id}/read`

**Response:**
- `404 Not Found`
- `{"message":"Notification not found","error":"Not Found"}`

**Verdict:** PASS — the notification belongs to org 1, and the org-2 request couldn't find it. No write leak.

## Summary

| Probe | Expected | Actual | Verdict |
|---|---|---|---|
| Cross-tenant GET | `total: 0` or `403` | `total: 0` | ✅ PASS |
| Cross-tenant WRITE | `404` or `403` | `404` | ✅ PASS |