package com.property.admin.service;

import com.platform.common.dtos.admin.LeaseStatsDto;
import com.platform.common.dtos.admin.MaintenanceStatsDto;
import com.platform.common.dtos.admin.PaymentSummaryDto;
import com.platform.common.dtos.admin.PropertyStatsDto;
import com.property.admin.client.AuthClient;
import com.property.admin.client.LeaseClient;
import com.property.admin.client.MaintenanceClient;
import com.property.admin.client.PaymentClient;
import com.property.admin.client.PropertyClient;
import com.property.admin.dto.OverviewDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.function.LongSupplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminOverviewService {

    private final AuthClient authClient;
    private final LeaseClient leaseClient;
    private final PaymentClient paymentClient;
    private final MaintenanceClient maintenanceClient;
    private final PropertyClient propertyClient;

    @Cacheable(value = "adminOverview", key = "'overview'")
    public OverviewDto getOverview() {
        log.debug("Building admin overview (cache miss)");

        long totalUsers = safeCount(() -> authClient.countUsers(null));
        long totalLandlords = safeCount(() -> authClient.countUsers("LANDLORD"));
        long totalTenants = safeCount(() -> authClient.countUsers("TENANT"));

        LeaseStatsDto leases = leaseClient.stats();
        MaintenanceStatsDto maintenance = maintenanceClient.stats();
        PaymentSummaryDto payments = paymentClient.summary();
        PropertyStatsDto propertyStats = propertyClient.stats();

        return new OverviewDto(
                totalUsers,
                totalUsers,
                totalLandlords,
                totalTenants,
                leases.totalActive(),
                maintenance.openTickets(),
                propertyStats,
                payments,
                0L
        );
    }

    private long safeCount(LongSupplier supplier) {
        try { return supplier.getAsLong(); }
        catch (Exception e) {
            log.warn("Count call failed: {}", e.getMessage());
            return 0L;
        }
    }
}