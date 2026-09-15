CREATE TABLE IF NOT EXISTS payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_id UUID REFERENCES invoices(id),
    tenant_id UUID NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    method VARCHAR(20) NOT NULL CHECK (method IN ('ach', 'credit_card', 'digital_wallet', 'cash', 'check')),
    gateway_transaction_id VARCHAR(100),
    status VARCHAR(20) DEFAULT 'initiated' CHECK (status IN ('initiated', 'pending', 'success', 'failed', 'refunded')),
    applied_to_invoice BOOLEAN DEFAULT FALSE,
    gateway_response JSONB,
    processed_at TIMESTAMP DEFAULT NOW(),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);