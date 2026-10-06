package com.platform.common.dtos.admin;

import java.io.Serializable;

public record MaintenanceStatsDto(
        long openTickets,
        long inProgress,
        long resolvedThisMonth,
        long slaBreach
) implements Serializable {

    private static final long serialVersionUID = 1L;

    public static MaintenanceStatsDto empty() {
        return new MaintenanceStatsDto(0L, 0L, 0L, 0L);
    }
}
