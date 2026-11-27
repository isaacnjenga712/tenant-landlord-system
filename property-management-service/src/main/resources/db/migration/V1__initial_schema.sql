-- V1__initial_schema.sql
-- Idempotent: uses IF NOT EXISTS so this migration can re-run safely
-- even if some tables were created by ddl-auto: update before Flyway
-- was enabled.

CREATE TABLE IF NOT EXISTS landlords (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS properties (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    address TEXT NOT NULL,
    landlord_id UUID REFERENCES landlords(id) ON DELETE RESTRICT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS units (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    property_id UUID REFERENCES properties(id) ON DELETE CASCADE,
    landlord_id UUID REFERENCES landlords(id) ON DELETE RESTRICT,
    unit_number VARCHAR(50) NOT NULL,
    square_footage INT,
    base_rent NUMERIC(12, 2) NOT NULL,
    operational_status VARCHAR(30) DEFAULT 'OPERATIONAL',
    CONSTRAINT unique_property_unit UNIQUE(property_id, unit_number)
);

-- Indexes (also idempotent)
CREATE INDEX IF NOT EXISTS idx_landlords_email ON landlords (email);
CREATE INDEX IF NOT EXISTS idx_properties_landlord_id ON properties (landlord_id);
CREATE INDEX IF NOT EXISTS idx_units_property_id ON units (property_id);
CREATE INDEX IF NOT EXISTS idx_units_landlord_id ON units (landlord_id);