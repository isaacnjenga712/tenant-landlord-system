package com.property.admin.client;

import com.platform.common.dtos.admin.MaintenanceStatsDto;
import com.property.admin.client.fallback.MaintenanceClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "MAINTENANCE-TICKET-SERVICE",
        path = "/api/v1/internal",
        fallback = MaintenanceClientFallback.class
)
public interface MaintenanceClient {

    @GetMapping("/tickets/stats")
    MaintenanceStatsDto stats();
}