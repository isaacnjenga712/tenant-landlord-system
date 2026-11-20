-- V4__landlord_email_unique_per_org.sql
-- Change landlords.email uniqueness from global to per-org.

ALTER TABLE landlords DROP CONSTRAINT IF EXISTS landlords_email_key;
ALTER TABLE landlords DROP CONSTRAINT IF EXISTS ukaygexby23xvqj9ctcegnuil65;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'landlords_org_email_key'
          AND conrelid = 'landlords'::regclass
    ) THEN
        ALTER TABLE landlords ADD CONSTRAINT landlords_org_email_key UNIQUE (org_id, email);
    END IF;
END $$;