-- This file is only for SQL-based databases (H2, PostgreSQL, etc.)
-- It is ignored when using MongoDB.

CREATE TABLE IF NOT EXISTS user_preference (
    user_id BIGINT PRIMARY KEY,
    email_opt_in BOOLEAN NOT NULL,
    sms_opt_in BOOLEAN NOT NULL,
    in_app_opt_in BOOLEAN NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS notification_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    channel VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    event_type VARCHAR(50),
    payload CLOB,
    attempts INT DEFAULT 0,
    error_message VARCHAR(500),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Insert sample user (for testing)
INSERT INTO user_preference (user_id, email_opt_in, sms_opt_in, in_app_opt_in, email, phone)
VALUES (1, true, true, true, 'tenant1@example.com', '+12345678901');