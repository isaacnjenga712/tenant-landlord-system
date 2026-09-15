CREATE TABLE IF NOT EXISTS security_deposits (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lease_id UUID NOT NULL UNIQUE,
    total_deposit DECIMAL(12,2) NOT NULL,
    held_in_account_id UUID REFERENCES payment_accounts(id),
    current_balance DECIMAL(12,2) NOT NULL,
    interest_accrued DECIMAL(12,2) DEFAULT 0.00,
    deduction_total DECIMAL(12,2) DEFAULT 0.00,
    returned_date DATE,
    return_amount DECIMAL(12,2),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_security_deposits_lease ON security_deposits(lease_id);
CREATE INDEX idx_security_deposits_held_account ON security_deposits(held_in_account_id);