-- V4__add_org_id_to_users.sql
-- Introduces SaaS tenant scoping (org_id) to users.
-- Idempotent: safe to re-run.

ALTER TABLE users ADD COLUMN IF NOT EXISTS org_id UUID;

UPDATE users
SET org_id = '00000000-0000-0000-0000-000000000001'
WHERE org_id IS NULL;

DO $$
DECLARE
    orphan_count bigint;
BEGIN
    SELECT count(*) INTO orphan_count FROM users WHERE org_id IS NULL;
    IF orphan_count > 0 THEN
        RAISE EXCEPTION 'Cannot set NOT NULL - % users have no org_id', orphan_count;
    END IF;
END $$;

ALTER TABLE users ALTER COLUMN org_id SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_users_org_id ON users (org_id);