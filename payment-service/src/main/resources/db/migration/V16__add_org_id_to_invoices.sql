-- V16__add_org_id_to_invoices.sql
-- Adds SaaS tenant scoping (org_id) to invoices.
-- Also changes invoice_number uniqueness from global to per-org.

ALTER TABLE invoices ADD COLUMN org_id UUID;
UPDATE invoices SET org_id = '00000000-0000-0000-0000-000000000001' WHERE org_id IS NULL;
ALTER TABLE invoices ALTER COLUMN org_id SET NOT NULL;

-- Drop global invoice_number unique, replace with per-org
ALTER TABLE invoices DROP CONSTRAINT IF EXISTS invoices_invoice_number_key;
ALTER TABLE invoices DROP CONSTRAINT IF EXISTS uk_invoices_invoice_number;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'invoices_org_number_key'
          AND conrelid = 'invoices'::regclass
    ) THEN
        ALTER TABLE invoices ADD CONSTRAINT invoices_org_number_key UNIQUE (org_id, invoice_number);
    END IF;
END $$;

-- Org-first indexes
CREATE INDEX IF NOT EXISTS idx_invoices_org_id ON invoices (org_id);
CREATE INDEX IF NOT EXISTS idx_invoices_org_lease ON invoices (org_id, lease_id);
CREATE INDEX IF NOT EXISTS idx_invoices_org_status ON invoices (org_id, status);
CREATE INDEX IF NOT EXISTS idx_invoices_org_due ON invoices (org_id, due_date);