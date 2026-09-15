-- V1__create_transaction_table.sql
-- Creates the transactions table with UUID primary key and reference columns.

CREATE TABLE IF NOT EXISTS transactions (
    id VARCHAR(36) PRIMARY KEY,
    checkout_request_id VARCHAR(50) UNIQUE NOT NULL,
    merchant_request_id VARCHAR(50),
    phone_number VARCHAR(15),
    amount NUMERIC(10, 2),
    mpesa_receipt_number VARCHAR(30),
    result_description TEXT,
    result_code INTEGER,
    status VARCHAR(20) NOT NULL,
    account_reference VARCHAR(50),
    tenant_id VARCHAR(36),
    lease_id VARCHAR(36),
    invoice_id VARCHAR(36),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

COMMENT ON TABLE transactions IS 'Stores all M-Pesa STK push transactions and their statuses.';
COMMENT ON COLUMN transactions.id IS 'Internal UUID primary key.';
COMMENT ON COLUMN transactions.checkout_request_id IS 'Safaricom''s unique request ID for idempotency.';
COMMENT ON COLUMN transactions.tenant_id IS 'UUID of the tenant (from landlord system).';
COMMENT ON COLUMN transactions.lease_id IS 'UUID of the lease (from landlord system), optional.';
COMMENT ON COLUMN transactions.invoice_id IS 'UUID of the invoice (from landlord system), optional.';