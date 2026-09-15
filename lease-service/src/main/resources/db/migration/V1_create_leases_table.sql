-- Enable UUID extension if not already enabled
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create the leases table
CREATE TABLE leases (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    property_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    landlord_id UUID NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    rent_amount DECIMAL(19, 2) NOT NULL,
    deposit_amount DECIMAL(19, 2),
    status VARCHAR(20) NOT NULL,
    terms_and_conditions TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Add a check constraint to restrict status values
ALTER TABLE leases
ADD CONSTRAINT chk_lease_status CHECK (
    status IN ('DRAFT', 'ACTIVE', 'EXPIRED', 'TERMINATED', 'CANCELLED')
);

-- Optional: Add a comment on the table and columns
COMMENT ON TABLE leases IS 'Stores lease agreements between tenants and landlords';
COMMENT ON COLUMN leases.id IS 'UUID primary key';
COMMENT ON COLUMN leases.property_id IS 'UUID referencing the property (external)';
COMMENT ON COLUMN leases.tenant_id IS 'UUID referencing the tenant (external)';
COMMENT ON COLUMN leases.landlord_id IS 'UUID referencing the landlord (external)';
COMMENT ON COLUMN leases.status IS 'Lease status: DRAFT, ACTIVE, EXPIRED, TERMINATED, CANCELLED';