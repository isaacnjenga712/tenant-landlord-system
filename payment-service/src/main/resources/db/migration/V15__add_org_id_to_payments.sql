-- V15__add_org_id_to_payments.sql
-- Adds SaaS tenant scoping (org_id) to payments.

ALTER TABLE payments ADD COLUMN org_id UUID;

UPDATE payments SET org_id = '00000000-0000-0000-0000-000000000001' WHERE org_id IS NULL;

ALTER TABLE payments ALTER COLUMN org_id SET NOT NULL;

CREATE INDEX idx_payments_org_id ON payments (org_id);
CREATE INDEX idx_payments_org_tenant ON payments (org_id, tenant_id);
CREATE INDEX idx_payments_org_invoice ON payments (org_id, invoice_id);
CREATE INDEX idx_payments_org_status ON payments (org_id, status);
CREATE INDEX idx_payments_org_created ON payments (org_id, created_at DESC);