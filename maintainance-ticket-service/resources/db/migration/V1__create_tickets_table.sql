-- Create tickets table
CREATE TABLE IF NOT EXISTS tickets (
    id  UUID VARCHAR(36) PRIMARY KEY,
    unit_id VARCHAR(36) NOT NULL,
    tenant_id VARCHAR(36) NOT NULL,
    landlord_id VARCHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    priority VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index on unit_id for faster lookups
CREATE INDEX IF NOT EXISTS idx_tickets_unit_id ON tickets(unit_id);

-- Create index on status for filtering
CREATE INDEX IF NOT EXISTS idx_tickets_status ON tickets(status);