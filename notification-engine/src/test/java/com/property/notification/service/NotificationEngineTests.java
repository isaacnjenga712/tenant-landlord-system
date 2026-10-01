package com.property.notification.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.property.notification.domain.NotificationEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lightweight unit tests for EventRouter + NotificationEventType.
 *
 * The full Kafka → dispatcher → channel flow is verified manually
 * (see log output + MongoDB) because embedding Kafka + Mongo in tests
 * adds far more runtime cost than it catches in bugs.
 */
class NotificationEngineTests {

    private EventRouter router;
    private ObjectMapper mapper;

    @BeforeEach
    void setup() {
        router = new EventRouter();
        mapper = new ObjectMapper();
    }

    // ---------- NotificationEventType.fromRaw ----------

    @Test
    void fromRaw_normalizes_dotted_and_flat_forms() {
        assertThat(NotificationEventType.fromRaw("maintenance.ticket.created"))
                .isEqualTo(NotificationEventType.MAINTENANCE_CREATED);
        assertThat(NotificationEventType.fromRaw("MAINTENANCE_CREATED"))
                .isEqualTo(NotificationEventType.MAINTENANCE_CREATED);
        assertThat(NotificationEventType.fromRaw("maintenance.ticket.resolved"))
                .isEqualTo(NotificationEventType.MAINTENANCE_RESOLVED);
        assertThat(NotificationEventType.fromRaw("lease.approved"))
                .isEqualTo(NotificationEventType.LEASE_APPROVED);
        assertThat(NotificationEventType.fromRaw("invoice.created"))
                .isEqualTo(NotificationEventType.INVOICE_CREATED);
    }

    @Test
    void fromRaw_returns_unknown_for_null_or_garbage() {
        assertThat(NotificationEventType.fromRaw(null)).isEqualTo(NotificationEventType.UNKNOWN);
        assertThat(NotificationEventType.fromRaw("")).isEqualTo(NotificationEventType.UNKNOWN);
        assertThat(NotificationEventType.fromRaw("some.random.thing"))
                .isEqualTo(NotificationEventType.UNKNOWN);
    }

    // ---------- EventRouter ----------

    @Test
    void route_extracts_tenant_and_landlord_ids() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID landlordId = UUID.randomUUID();
        String json = """
                {
                  "tenantId": "%s",
                  "landlordId": "%s",
                  "title": "AC not working"
                }
                """.formatted(tenantId, landlordId);

        JsonNode node = mapper.readTree(json);
        EventRouter.RoutedEvent routed =
                router.route(NotificationEventType.MAINTENANCE_CREATED, node);

        assertThat(routed.getTenantId()).isEqualTo(tenantId);
        assertThat(routed.getLandlordId()).isEqualTo(landlordId);
        assertThat(routed.getSummary()).contains("AC not working");
    }

    @Test
    void route_falls_back_to_userId_when_tenantId_missing() throws Exception {
        UUID userId = UUID.randomUUID();
        String json = """
                {
                  "userId": "%s",
                  "title": "login"
                }
                """.formatted(userId);

        JsonNode node = mapper.readTree(json);
        EventRouter.RoutedEvent routed =
                router.route(NotificationEventType.USER_LOGIN, node);

        assertThat(routed.getTenantId()).isEqualTo(userId);
    }

    @Test
    void route_handles_missing_recipients_gracefully() throws Exception {
        String json = """
                {
                  "eventType": "user.login"
                }
                """;

        JsonNode node = mapper.readTree(json);
        EventRouter.RoutedEvent routed =
                router.route(NotificationEventType.USER_LOGIN, node);

        assertThat(routed.getTenantId()).isNull();
        assertThat(routed.getLandlordId()).isNull();
        assertThat(routed.getSummary()).isNotBlank();
    }

    @Test
    void route_lease_approved_produces_expected_summary() throws Exception {
        UUID tenantId = UUID.randomUUID();
        String json = """
                {
                  "tenantId": "%s",
                  "landlordId": "%s",
                  "leaseId": "%s"
                }
                """.formatted(tenantId, UUID.randomUUID(), UUID.randomUUID());

        JsonNode node = mapper.readTree(json);
        EventRouter.RoutedEvent routed =
                router.route(NotificationEventType.LEASE_APPROVED, node);

        assertThat(routed.getSummary()).containsIgnoringCase("approved");
        assertThat(routed.getTenantId()).isEqualTo(tenantId);
    }

    @Test
    void route_payment_received_summary_mentions_mpesa() throws Exception {
        UUID tenantId = UUID.randomUUID();
        String json = """
                {
                  "tenantId": "%s",
                  "amount": 24000
                }
                """.formatted(tenantId);

        JsonNode node = mapper.readTree(json);
        EventRouter.RoutedEvent routed =
                router.route(NotificationEventType.PAYMENT_RECEIVED_MPESA, node);

        assertThat(routed.getSummary()).containsIgnoringCase("M-Pesa");
    }
}