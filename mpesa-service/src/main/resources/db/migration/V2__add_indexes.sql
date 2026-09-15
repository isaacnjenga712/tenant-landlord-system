-- V2__add_indexes.sql
-- Adds indexes to speed up queries on frequently filtered columns.

-- Index for the tenant_id column (used in getTransactionsByTenant)
CREATE INDEX IF NOT EXISTS idx_transactions_tenant_id ON transactions(tenant_id);

-- Index for the lease_id column (used in getTransactionsByLease)
CREATE INDEX IF NOT EXISTS idx_transactions_lease_id ON transactions(lease_id);

-- Index for the checkout_request_id (already unique, but we keep for fast lookups)
CREATE INDEX IF NOT EXISTS idx_transactions_checkout_request_id ON transactions(checkout_request_id);

-- Index for the status column (filtering by PENDING/SUCCESS/FAILED)
CREATE INDEX IF NOT EXISTS idx_transactions_status ON transactions(status);

-- Index for the created_at column (used in date-range queries)
CREATE INDEX IF NOT EXISTS idx_transactions_created_at ON transactions(created_at);