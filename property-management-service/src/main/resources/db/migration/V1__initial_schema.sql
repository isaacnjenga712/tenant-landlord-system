CREATE TABLE landlords (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20)
);
CREATE TABLE properties (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    address TEXT NOT NULL,
    landlord_id UUID REFERENCES landlords(id) ON DELETE RESTRICT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE units (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    property_id UUID REFERENCES properties(id) ON DELETE CASCADE,
    landlord_id UUID REFERENCES landlords(id) ON DELETE RESTRICT, 
    unit_number VARCHAR(50) NOT NULL,
    square_footage INT,
    base_rent NUMERIC(12, 2) NOT NULL,
    operational_status VARCHAR(30) DEFAULT 'OPERATIONAL',
    CONSTRAINT unique_property_unit UNIQUE(property_id, unit_number)
);