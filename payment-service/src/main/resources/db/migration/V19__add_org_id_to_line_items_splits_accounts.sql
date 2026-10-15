-- V19__add_org_id_to_line_items_splits_accounts.sql
-- Adds SaaS tenant scoping (org_id) to invoice_line_items, payment_splits, payment_accounts.

-- =================== invoice_line_items ===================
ALTER TABLE invoice_line_items ADD COLUMN org_id UUID;
UPDATE invoice_line_items SET org_id = '00000000-0000-0000-0000-000000000001' WHERE org_id IS NULL;
ALTER TABLE invoice_line_items ALTER COLUMN org_id SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_line_items_org_id ON invoice_line_items (org_id);
CREATE INDEX IF NOT EXISTS idx_line_items_org_invoice ON invoice_line_items (org_id, invoice_id);

-- =================== payment_splits ===================
ALTER TABLE payment_splits ADD COLUMN org_id UUID;
UPDATE payment_splits SET org_id = '00000000-0000-0000-0000-000000000001' WHERE org_id IS NULL;
ALTER TABLE payment_splits ALTER COLUMN org_id SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_splits_org_id ON payment_splits (org_id);
CREATE INDEX IF NOT EXISTS idx_splits_org_payment ON payment_splits (org_id, payment_id);
CREATE INDEX IF NOT EXISTS idx_splits_org_invoice ON payment_splits (org_id, invoice_id);

-- =================== payment_accounts ===================
ALTER TABLE payment_accounts ADD COLUMN org_id UUID;
UPDATE payment_accounts SET org_id = '00000000-0000-0000-0000-000000000001' WHERE org_id IS NULL;
ALTER TABLE payment_accounts ALTER COLUMN org_id SET NOT NULL;

-- Change (entity_type, entity_id) unique to (org_id, entity_type, entity_id)
ALTER TABLE payment_accounts DROP CONSTRAINT IF EXISTS uk_entity_type_id;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uk_org_entity_type_id'
          AND conrelid = 'payment_accounts'::regclass
    ) THEN
        ALTER TABLE payment_accounts ADD CONSTRAINT uk_org_entity_type_id
            UNIQUE (org_id, entity_type, entity_id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_accounts_org_id ON payment_accounts (org_id);
CREATE INDEX IF NOT EXISTS idx_accounts_org_entity ON payment_accounts (org_id, entity_type, entity_id);