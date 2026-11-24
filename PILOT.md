# Pilot Test — RentFlow Multi-Tenant Isolation

## Goal

Prove that a small group of users can complete the core RentFlow workflow
(register → login → inbox → mark read) without cross-tenant data leakage,
while audit logs capture security events and log volume stays manageable.

## Scope

**In:**
- Auth service (register, login, refresh)
- Notification engine (inbox, mark read)
- Audit log (security_audit_logs)
- Cross-tenant probes (manual)

**Out:**
- Kafka event publishing (deferred)
- Payment/lease/property services (separate pilots)
- New feature development

## Success Measures

| Measure | Target | Method |
|---|---|---|
| Cross-tenant leaks | 0 | Manual probes (org 2 header + org 1 token) |
| Audit log coverage | 100% of login/register | Query `security_audit_logs` |
| Log volume | < 50 lines per session | `docker logs --since` |
| Workflow completion | 5/5 without support | Manual test |
| Load test regression | 0 leaks, 0 inconclusive | k6 at 10 VUs |

## Environment

- **Database:** Separate `pilot` schema (or fresh DB)
- **Data:** Fake users (`pilot1@example.com`, `pilot2@example.com`)
- **Org:** Org 1 only (no cross-tenant user mixing)
- **Access:** You (admin) + 1–2 test users

## Schedule

- Day 1: Prepare (3–4h)
- Day 2: Active usage (2h)
- Day 3: Cross-tenant probes (2h)
- Day 4: Evaluate + fix (2–3h)
- Day 5: Decision + document (1–2h)

## Evidence

(To be filled in during the pilot — audit log queries, probe results, log volume)