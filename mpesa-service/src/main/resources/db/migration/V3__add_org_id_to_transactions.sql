-- V3__add_org_id_to_transactions.sql
-- Adds SaaS tenant scoping (org_id) to the transactions table.
-- String type matches existing tenant_id/lease_id columns.

ALTER TABLE transactions ADD COLUMN IF NOT EXISTS org_id VARCHAR(36);

UPDATE transactions
SET org_id = '00000000-0000-0000-0000-000000000001'
WHERE org_id IS NULL;

ALTER TABLE transactions ALTER COLUMN org_id SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_transactions_org_id       ON transactions (org_id);
CREATE INDEX IF NOT EXISTS idx_transactions_org_tenant   ON transactions (org_id, tenant_id);
CREATE INDEX IF NOT EXISTS idx_transactions_org_status   ON transactions (org_id, status);
CREATE INDEX IF NOT EXISTS idx_transactions_org_checkout ON transactions (org_id, checkout_request_id);