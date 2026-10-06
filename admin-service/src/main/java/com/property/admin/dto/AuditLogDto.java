package com.property.admin.dto;

import java.time.Instant;

public record AuditLogDto(
        String id,
        String actorEmail,
        String action,
        String resource,
        String resourceId,
        String ipAddress,
        Instant createdAt
) {}
