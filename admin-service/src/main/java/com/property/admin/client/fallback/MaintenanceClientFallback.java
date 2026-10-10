package com.property.admin.client.fallback;

import com.platform.common.dtos.admin.MaintenanceStatsDto;
import com.property.admin.client.MaintenanceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MaintenanceClientFallback implements MaintenanceClient {

    @Override
    public MaintenanceStatsDto stats() {
        log.warn("maintenance-service unavailable — returning empty stats");
        return MaintenanceStatsDto.empty();
    }
}