package com.property.admin.dto;

import com.platform.common.dtos.admin.PaymentSummaryDto;
import com.platform.common.dtos.admin.PropertyStatsDto;

import java.io.Serializable;

public record OverviewDto(
        long totalUsers,
        long activeUsers,
        long totalLandlords,
        long totalTenants,
        long activeLeases,
        long openTickets,
        PropertyStatsDto propertyStats,         // ← NEW
        PaymentSummaryDto paymentSummary,
        long unreadNotifications
) implements Serializable {

    private static final long serialVersionUID = 1L;
}
