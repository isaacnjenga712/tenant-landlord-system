 -- V2__add_org_id_to_tickets_and_tenants.sql
-- Adds SaaS tenant scoping (org_id) to tickets + tenants.
-- ============ TENANTS ============
CREATE TABLE IF NOT EXISTS tenants (
    id UUID PRIMARY KEY,
    org_id UUID,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    created_at TIMESTAMP
);

ALTER TABLE tenants ADD COLUMN IF NOT EXISTS org_id UUID;
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

-- ============ TICKETS ============
CREATE TABLE IF NOT EXISTS tickets (
    id UUID PRIMARY KEY,
    org_id UUID,
    unit_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    landlord_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    priority VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

ALTER TABLE tickets ADD COLUMN IF NOT EXISTS org_id UUID;
UPDATE tickets SET org_id = '00000000-0000-0000-0000-000000000001' WHERE org_id IS NULL;
ALTER TABLE tickets ALTER COLUMN org_id SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_tickets_org_id       ON tickets (org_id);
CREATE INDEX IF NOT EXISTS idx_tickets_org_unit     ON tickets (org_id, unit_id);
CREATE INDEX IF NOT EXISTS idx_tickets_org_tenant   ON tickets (org_id, tenant_id);
CREATE INDEX IF NOT EXISTS idx_tickets_org_landlord ON tickets (org_id, landlord_id);
CREATE INDEX IF NOT EXISTS idx_tickets_org_status   ON tickets (org_id, status);