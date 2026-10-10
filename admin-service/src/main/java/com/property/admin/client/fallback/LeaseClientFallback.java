package com.property.admin.client.fallback;

import com.platform.common.dtos.admin.LeaseStatsDto;
import com.property.admin.client.LeaseClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LeaseClientFallback implements LeaseClient {

    @Override
    public LeaseStatsDto stats() {
        log.warn("lease-service unavailable — returning empty stats");
        return LeaseStatsDto.empty();
    }
}