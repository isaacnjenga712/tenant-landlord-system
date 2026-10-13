-- V2__add_org_id_to_leases.sql

ALTER TABLE leases ADD COLUMN org_id UUID;

UPDATE leases
SET org_id = '00000000-0000-0000-0000-000000000001'
WHERE org_id IS NULL;

DO $$
DECLARE
    orphan_count bigint;
BEGIN
    SELECT count(*) INTO orphan_count FROM leases WHERE org_id IS NULL;
    IF orphan_count > 0 THEN
        RAISE EXCEPTION 'Cannot set NOT NULL - % leases have no org_id', orphan_count;
    END IF;
END $$;

ALTER TABLE leases ALTER COLUMN org_id SET NOT NULL;

CREATE INDEX idx_leases_org_id         ON leases (org_id);
CREATE INDEX idx_leases_org_id_id      ON leases (org_id, id);
CREATE INDEX idx_leases_org_property   ON leases (org_id, property_id);
CREATE INDEX idx_leases_org_tenant     ON leases (org_id, tenant_id);
CREATE INDEX idx_leases_org_landlord   ON leases (org_id, landlord_id);
CREATE INDEX idx_leases_org_status     ON leases (org_id, status);
CREATE INDEX idx_leases_org_dates      ON leases (org_id, start_date, end_date);