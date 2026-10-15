-- V18__add_org_id_to_tenants.sql
-- Adds SaaS tenant scoping to the duplicated tenants table.
-- Long-term this table will be dropped (see refactor ticket: replace with
-- Feign client to auth-service). For now, all rows belong to the placeholder org.

ALTER TABLE tenants ADD COLUMN org_id UUID;
UPDATE tenants SET org_id = '00000000-0000-0000-0000-000000000001' WHERE org_id IS NULL;
ALTER TABLE tenants ALTER COLUMN org_id SET NOT NULL;

-- Global email unique -> per-org
ALTER TABLE tenants DROP CONSTRAINT IF EXISTS tenants_email_key;
ALTER TABLE tenants DROP CONSTRAINT IF EXISTS uk_tenants_email;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'tenants_org_email_key'
          AND conrelid = 'tenants'::regclass
    ) THEN
        ALTER TABLE tenants ADD CONSTRAINT tenants_org_email_key UNIQUE (org_id, email);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_tenants_org_id ON tenants (org_id);
CREATE INDEX IF NOT EXISTS idx_tenants_org_status ON tenants (org_id, status);