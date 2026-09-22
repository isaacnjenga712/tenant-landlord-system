
CREATE TABLE IF NOT EXISTS refresh_tokens (

    id BIGSERIAL PRIMARY KEY,

    token VARCHAR(500) NOT NULL UNIQUE,

    user_id BIGINT NOT NULL,

    expiry_date TIMESTAMP NOT NULL,

    revoked BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP

);

CREATE INDEX IF NOT EXISTS idx_refresh_token_user_id ON refresh_tokens(user_id);

CREATE INDEX IF NOT EXISTS idx_refresh_token_token ON refresh_tokens(token);

-- Add FK later after users exists, safely

DO $$ BEGIN

  ALTER TABLE refresh_tokens ADD CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

EXCEPTION WHEN OTHERS THEN NULL;

END $$;

