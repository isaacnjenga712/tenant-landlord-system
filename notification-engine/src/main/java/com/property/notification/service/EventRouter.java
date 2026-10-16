package com.property.notification.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.property.notification.domain.NotificationEventType;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class EventRouter {

    @Data
    @Builder
    public static class RoutedEvent {
        private NotificationEventType type;
        private UUID organizationId;   // NEW
        private UUID tenantId;
        private UUID landlordId;
        private String summary;
        private String detail;
    }

    public RoutedEvent route(NotificationEventType type, JsonNode payload) {
        UUID organizationId = extractUuid(payload, "organizationId", "orgId");
        UUID tenantId       = extractUuid(payload, "tenantId", "userId", "publicId");
        UUID landlordId     = extractUuid(payload, "landlordId");

        String summary;
        String detail;
        switch (type) {
            case LEASE_CREATED -> {
                summary = "Your lease application has been received.";
                detail = "The landlord will review it shortly.";
            }
            // ... unchanged ...
            default -> {
                summary = "New notification from RentFlow.";
                detail = summary;
            }
        }

        return RoutedEvent.builder()
                .type(type)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .landlordId(landlordId)
                .summary(summary)
                .detail(detail)
                .build();
    }

    private UUID extractUuid(JsonNode node, String... fieldNames) {
        for (String f : fieldNames) {
            JsonNode v = node.path(f);
            if (v.isMissingNode() || v.isNull()) continue;
            String text = v.asText(null);
            if (text == null || text.isBlank()) continue;
            try { return UUID.fromString(text); } catch (IllegalArgumentException ignored) {}
        }
        return null;
    }
}