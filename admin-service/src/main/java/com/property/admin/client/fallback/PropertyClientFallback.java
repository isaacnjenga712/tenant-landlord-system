package com.property.admin.client.fallback;

import com.platform.common.dtos.admin.PropertyStatsDto;
import com.property.admin.client.PropertyClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PropertyClientFallback implements PropertyClient {

    @Override
    public PropertyStatsDto stats() {
        log.warn("property-management-service unavailable — returning empty stats");
        return PropertyStatsDto.empty();
    }
}
