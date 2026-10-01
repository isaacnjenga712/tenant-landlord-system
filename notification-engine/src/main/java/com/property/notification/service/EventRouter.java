package com.property.notification.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.property.notification.domain.NotificationEventType;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class EventRouter {

    @Data
    @Builder
    public static class RoutedEvent {
        private NotificationEventType type;
        private UUID tenantId;
        private UUID landlordId;
        /** Human-readable summary used for SMS/WhatsApp/In-App bodies. */
        private String summary;
        /** Optional longer body for email. */
        private String detail;
    }

    /**
     * Given a canonical event type and its raw JSON payload,
     * pull out the recipient UUIDs and build a display message.
     */
    public RoutedEvent route(NotificationEventType type, JsonNode payload) {
        UUID tenantId = extractUuid(payload, "tenantId", "userId", "publicId");
        UUID landlordId = extractUuid(payload, "landlordId");

        String summary;
        String detail;
        switch (type) {
            case LEASE_CREATED -> {
                summary = "Your lease application has been received.";
                detail = "The landlord will review it shortly.";
            }
            case LEASE_APPROVED -> {
                summary = "Your lease has been approved.";
                detail = "Welcome home! You can view the lease in the app.";
            }
            case LEASE_TERMINATED -> {
                summary = "Your lease has been terminated.";
                detail = "Check the app for details.";
            }
            case INVOICE_CREATED -> {
                summary = "A new invoice has been issued.";
                detail = "Check your invoices in the app to see the details.";
            }
            case PAYMENT_RECEIVED_MPESA -> {
                summary = "M-Pesa payment received.";
                detail = summary + " Thank you — your account has been updated.";
            }
            case MAINTENANCE_CREATED -> {
                String title = payload.path("title").asText("a maintenance request");
                summary = "New maintenance request: " + title;
                detail = "A tenant reported: " + title;
            }
            case MAINTENANCE_RESOLVED -> {
                String title = payload.path("title").asText("your request");
                summary = "Maintenance resolved: " + title;
                detail = "Your maintenance request \"" + title + "\" has been resolved.";
            }
            case USER_REGISTERED -> {
                summary = "Welcome to RentFlow.";
                detail = "Your account is ready.";
            }
            case USER_LOGIN -> {
                summary = "New login detected.";
                detail = "A login to your account just happened.";
            }
            default -> {
                summary = "New notification from RentFlow.";
                detail = summary;
            }
        }

        return RoutedEvent.builder()
                .type(type)
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
            try {
                return UUID.fromString(text);
            } catch (IllegalArgumentException ignored) { }
        }
        return null;
    }
}
