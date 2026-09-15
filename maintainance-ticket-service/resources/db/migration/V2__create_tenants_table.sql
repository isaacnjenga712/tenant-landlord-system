-- ============================================================
-- V2__create_tenants_table.sql
-- Description: Creates the tenants table to store tenant information
-- ============================================================

CREATE TABLE IF NOT EXISTS tenants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index on email for fast lookups
CREATE INDEX IF NOT EXISTS idx_tenants_email ON tenants(email);

-- Create index on name for search
CREATE INDEX IF NOT EXISTS idx_tenants_name ON tenants(name);

-- Optional: Add a comment to the table
COMMENT ON TABLE tenants IS 'Stores tenant information for the property management system';