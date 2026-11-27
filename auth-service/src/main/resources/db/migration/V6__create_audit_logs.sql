-- V6__create_audit_logs.sql
-- Creates the security_audit_logs table with per-tenant retention support.
-- This is separate from the financial audit_logs table (owned by payment-service).

CREATE TABLE IF NOT EXISTS security_audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id UUID NOT NULL,
    actor_id UUID,
    event_type VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50),
    resource_id VARCHAR(255),
    payload_hash VARCHAR(64),
    ip_address VARCHAR(45),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_security_audit_org_created
    ON security_audit_logs (org_id, created_at);

CREATE INDEX IF NOT EXISTS idx_security_audit_event_type
    ON security_audit_logs (event_type);