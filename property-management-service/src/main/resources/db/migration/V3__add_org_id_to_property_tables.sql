-- V3__add_org_id_to_property_tables.sql
-- Adds SaaS tenant scoping (org_id) to landlords, properties, units.

ALTER TABLE landlords ADD COLUMN org_id UUID;

UPDATE landlords SET org_id = '00000000-0000-0000-0000-000000000001' WHERE org_id IS NULL;

ALTER TABLE landlords ALTER COLUMN org_id SET NOT NULL;

CREATE INDEX idx_landlords_org_id ON landlords (org_id);
CREATE INDEX idx_landlords_org_status ON landlords (org_id, status);

ALTER TABLE properties ADD COLUMN org_id UUID;

UPDATE properties SET org_id = '00000000-0000-0000-0000-000000000001' WHERE org_id IS NULL;

ALTER TABLE properties ALTER COLUMN org_id SET NOT NULL;

CREATE INDEX idx_properties_org_id ON properties (org_id);
CREATE INDEX idx_properties_org_landlord ON properties (org_id, landlord_id);
CREATE INDEX idx_properties_org_status ON properties (org_id, status);

ALTER TABLE units ADD COLUMN org_id UUID;

UPDATE units SET org_id = '00000000-0000-0000-0000-000000000001' WHERE org_id IS NULL;

ALTER TABLE units ALTER COLUMN org_id SET NOT NULL;

CREATE INDEX idx_units_org_id ON units (org_id);
CREATE INDEX idx_units_org_property ON units (org_id, property_id);
CREATE INDEX idx_units_org_status ON units (org_id, status);
CREATE INDEX idx_units_org_tenant ON units (org_id, current_tenant_id);