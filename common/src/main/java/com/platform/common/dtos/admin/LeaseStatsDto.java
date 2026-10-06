package com.platform.common.dtos.admin;

import java.io.Serializable;

/**
 * Lease KPI summary for the admin dashboard.
 * Returned by lease-service, consumed by admin-service.
 * Serializable so it can be cached in Redis via JDK serialization.
 */
public record LeaseStatsDto(
        long totalActive,
        long createdThisMonth,
        long terminatedThisMonth,
        long pendingApproval
) implements Serializable {

    private static final long serialVersionUID = 1L;

    public static LeaseStatsDto empty() {
        return new LeaseStatsDto(0L, 0L, 0L, 0L);
    }
}