-- V4__landlord_email_unique_per_org.sql
-- Change landlords.email and user_id uniqueness from global to per-org.
-- Original Hibernate-generated global constraints:
--   ukaygexby23xvqj9ctcegnuil65  → UNIQUE(email)
--   ukmwxg1mtc1u9yhxafv1qxb5mde  → UNIQUE(user_id)

ALTER TABLE landlords DROP CONSTRAINT IF EXISTS ukaygexby23xvqj9ctcegnuil65;
ALTER TABLE landlords DROP CONSTRAINT IF EXISTS ukmwxg1mtc1u9yhxafv1qxb5mde;
ALTER TABLE landlords DROP CONSTRAINT IF EXISTS landlords_email_key;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'landlords_org_email_key'
          AND conrelid = 'landlords'::regclass
    ) THEN
        ALTER TABLE landlords ADD CONSTRAINT landlords_org_email_key UNIQUE (org_id, email);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'landlords_org_user_key'
          AND conrelid = 'landlords'::regclass
    ) THEN
        ALTER TABLE landlords ADD CONSTRAINT landlords_org_user_key UNIQUE (org_id, user_id);
    END IF;
END $$;