CREATE TABLE IF NOT EXISTS deposit_deductions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    deposit_id UUID NOT NULL REFERENCES security_deposits(id) ON DELETE CASCADE,
    description TEXT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    supporting_document_url TEXT,
    approved_by UUID,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_deposit_deductions_deposit ON deposit_deductions(deposit_id);
CREATE INDEX idx_deposit_deductions_approved_by ON deposit_deductions(approved_by);