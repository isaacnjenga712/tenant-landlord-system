package com.platform.common.tenant;

import java.util.UUID;

/**
 * Holds the current SaaS tenant (org) for the duration of one request.
 * Set by TenantFilter from the X-Org-Id header, cleared in finally.
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT = new ThreadLocal<>();

    private TenantContext() {}

    public static void set(UUID orgId) {
        CURRENT.set(orgId);
    }

    public static UUID get() {
        return CURRENT.get();
    }

    public static UUID require() {
        UUID id = CURRENT.get();
        if (id == null) {
            throw new IllegalStateException("No tenant context — request missing X-Org-Id");
        }
        return id;
    }

    public static void clear() {
        CURRENT.remove();
    }
}
