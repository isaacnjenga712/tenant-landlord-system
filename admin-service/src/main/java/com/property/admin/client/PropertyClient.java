package com.property.admin.client;

import com.platform.common.dtos.admin.PropertyStatsDto;
import com.property.admin.client.fallback.PropertyClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "PROPERTY-MANAGEMENT-SERVICE",
        contextId = "adminPropertyClient",
        path = "/api/v1/internal",
        fallback = PropertyClientFallback.class
)
public interface PropertyClient {

    @GetMapping("/properties/stats")
    PropertyStatsDto stats();
}
