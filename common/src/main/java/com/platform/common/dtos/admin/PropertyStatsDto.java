package com.platform.common.dtos.admin;

import java.io.Serializable;

/**
 * Property + unit KPI summary for the admin dashboard.
 * Returned by property-management-service, consumed by admin-service.
 */
public record PropertyStatsDto(
        long totalProperties,
        long totalUnits,
        long occupiedUnits,
        long vacantUnits,
        double occupancyRate      // 0.0 – 1.0
) implements Serializable {

    private static final long serialVersionUID = 1L;

    public static PropertyStatsDto empty() {
        return new PropertyStatsDto(0L, 0L, 0L, 0L, 0.0);
    }
}