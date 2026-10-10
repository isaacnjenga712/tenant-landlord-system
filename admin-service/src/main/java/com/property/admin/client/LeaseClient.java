package com.property.admin.client;

import com.platform.common.dtos.admin.LeaseStatsDto;
import com.property.admin.client.fallback.LeaseClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "LEASE-SERVICE",
        path = "/api/v1/internal",
        fallback = LeaseClientFallback.class
)
public interface LeaseClient {

    @GetMapping("/leases/stats")
    LeaseStatsDto stats();
}
