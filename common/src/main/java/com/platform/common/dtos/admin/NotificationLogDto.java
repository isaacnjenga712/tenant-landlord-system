package com.platform.common.dtos.admin;

import java.time.Instant;

/**
 * Delivery log entry for the admin operations view.
 * Returned by notification-engine, consumed by admin-service.
 * Mirrors NotificationLog but decouples the domain entity from the wire format.
 */
public record NotificationLogDto(
        String id,
        String recipientPublicId,
        String eventType,
        String channel,
        String status,
        int attempts,
        String errorMessage,
        Instant createdAt
) {}
