CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL REFERENCES payment_accounts(id) ON DELETE CASCADE,
    action_type VARCHAR(20) NOT NULL CHECK (action_type IN ('CREATE', 'UPDATE', 'ADJUST', 'TRANSFER', 'DELETE')),
    amount_delta DECIMAL(12,2),
    before_trust_balance DECIMAL(12,2),
    after_trust_balance DECIMAL(12,2),
    before_operational_balance DECIMAL(12,2),
    after_operational_balance DECIMAL(12,2),
    reason TEXT,
    performed_at TIMESTAMP NOT NULL DEFAULT NOW(),
    metadata JSONB
);

CREATE INDEX idx_audit_logs_account ON audit_logs(account_id);
CREATE INDEX idx_audit_logs_performed_at ON audit_logs(performed_at);
CREATE INDEX idx_audit_logs_action ON audit_logs(action_type);