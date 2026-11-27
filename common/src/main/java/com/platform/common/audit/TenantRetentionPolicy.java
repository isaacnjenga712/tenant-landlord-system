package com.platform.common.audit;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Per-tenant retention policy for audit logs.
 *
 * <p>Default: 7 days (free tier). Enterprise orgs: 365 days.
 * The policy is configurable via environment variable or per-org override.
 */
@Component
public class TenantRetentionPolicy {

    /** Default retention: 7 days. */
    private static final int DEFAULT_RETENTION_DAYS = 7;

    /** Enterprise retention: 365 days. */
    private static final int ENTERPRISE_RETENTION_DAYS = 365;

    /** Enterprise orgs (hardcoded for demo — replace with DB lookup). */
    private static final java.util.Set<UUID> ENTERPRISE_ORGS = java.util.Set.of(
        // Add enterprise org IDs here
    );

    public int getRetentionDays(UUID orgId) {
        if (ENTERPRISE_ORGS.contains(orgId)) {
            return ENTERPRISE_RETENTION_DAYS;
        }
        return DEFAULT_RETENTION_DAYS;
    }

    public LocalDateTime getCutoff(UUID orgId) {
        return LocalDateTime.now().minusDays(getRetentionDays(orgId));
    }
}
