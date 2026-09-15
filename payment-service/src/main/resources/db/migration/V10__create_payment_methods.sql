CREATE TABLE IF NOT EXISTS payment_methods (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    gateway_customer_id VARCHAR(100) NOT NULL,
    gateway_payment_method_id VARCHAR(100) NOT NULL,
    type VARCHAR(20) CHECK (type IN ('card', 'bank_account')),
    last_four VARCHAR(4),
    expiry_month INT,
    expiry_year INT,
    is_default BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_payment_methods_tenant ON payment_methods(tenant_id);
CREATE INDEX idx_payment_methods_gateway_customer ON payment_methods(gateway_customer_id);
CREATE UNIQUE INDEX uk_payment_methods_gateway_method ON payment_methods(gateway_payment_method_id);