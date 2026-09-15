-- ============================================================
-- V1__create_tickets_table.sql
-- Description: Creates the tickets table for maintenance tickets
-- ============================================================

CREATE TABLE IF NOT EXISTS tickets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    unit_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    landlord_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    priority VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for faster lookups
CREATE INDEX IF NOT EXISTS idx_tickets_unit_id ON tickets(unit_id);
CREATE INDEX IF NOT EXISTS idx_tickets_tenant_id ON tickets(tenant_id);
CREATE INDEX IF NOT EXISTS idx_tickets_status ON tickets(status);
CREATE INDEX IF NOT EXISTS idx_tickets_priority ON tickets(priority);

-- Add a comment to the table
COMMENT ON TABLE tickets IS 'Stores maintenance tickets for properties and units';