/* V1 create payment accounts */

CREATE TABLE IF NOT EXISTS payment_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type VARCHAR(20) NOT NULL CHECK (entity_type IN ('tenant', 'landlord', 'property')),
    entity_id UUID NOT NULL,
    trust_balance DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    operational_balance DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_entity_type_id UNIQUE (entity_type, entity_id),
    CONSTRAINT chk_trust_non_negative CHECK (trust_balance >= 0)
);

CREATE INDEX idx_payment_accounts_entity ON payment_accounts(entity_type, entity_id);
CREATE INDEX idx_payment_accounts_active ON payment_accounts(is_active);