-- V20__add_org_id_to_methods_deposits_deductions_audit.sql
-- Adds SaaS tenant scoping (org_id) to payment_methods, security_deposits,
-- deposit_deductions, audit_logs.

-- ============ payment_methods ============
ALTER TABLE payment_methods ADD COLUMN org_id UUID;
UPDATE payment_methods SET org_id = '00000000-0000-0000-0000-000000000001' WHERE org_id IS NULL;
ALTER TABLE payment_methods ALTER COLUMN org_id SET NOT NULL;

-- Global unique on gateway_payment_method_id -> per-org
DROP INDEX IF EXISTS uk_payment_methods_gateway_method;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes
        WHERE indexname = 'uk_payment_methods_org_gateway_method'
    ) THEN
        CREATE UNIQUE INDEX uk_payment_methods_org_gateway_method
            ON payment_methods (org_id, gateway_payment_method_id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_payment_methods_org_id ON payment_methods (org_id);
CREATE INDEX IF NOT EXISTS idx_payment_methods_org_tenant ON payment_methods (org_id, tenant_id);

-- ============ security_deposits ============
ALTER TABLE security_deposits ADD COLUMN org_id UUID;
UPDATE security_deposits SET org_id = '00000000-0000-0000-0000-000000000001' WHERE org_id IS NULL;
ALTER TABLE security_deposits ALTER COLUMN org_id SET NOT NULL;

-- Global unique on lease_id -> per-org
ALTER TABLE security_deposits DROP CONSTRAINT IF EXISTS security_deposits_lease_id_key;
ALTER TABLE security_deposits DROP CONSTRAINT IF EXISTS uk_security_deposits_lease_id;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'security_deposits_org_lease_key'
          AND conrelid = 'security_deposits'::regclass
    ) THEN
        ALTER TABLE security_deposits ADD CONSTRAINT security_deposits_org_lease_key
            UNIQUE (org_id, lease_id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_security_deposits_org_id ON security_deposits (org_id);
CREATE INDEX IF NOT EXISTS idx_security_deposits_org_lease ON security_deposits (org_id, lease_id);

-- ============ deposit_deductions ============
ALTER TABLE deposit_deductions ADD COLUMN org_id UUID;
UPDATE deposit_deductions SET org_id = '00000000-0000-0000-0000-000000000001' WHERE org_id IS NULL;
ALTER TABLE deposit_deductions ALTER COLUMN org_id SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_deposit_deductions_org_id ON deposit_deductions (org_id);
CREATE INDEX IF NOT EXISTS idx_deposit_deductions_org_deposit ON deposit_deductions (org_id, deposit_id);

-- ============ audit_logs ============
ALTER TABLE audit_logs ADD COLUMN org_id UUID;
UPDATE audit_logs SET org_id = '00000000-0000-0000-0000-000000000001' WHERE org_id IS NULL;
ALTER TABLE audit_logs ALTER COLUMN org_id SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_audit_logs_org_id ON audit_logs (org_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_org_account ON audit_logs (org_id, account_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_org_performed ON audit_logs (org_id, performed_at);