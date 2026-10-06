package com.property.admin.dto;

import java.time.Instant;

public record ServiceHealthDto(
        String serviceName,
        String status,
        String version,
        String instanceId,
        Instant lastHeartbeat
) {}
