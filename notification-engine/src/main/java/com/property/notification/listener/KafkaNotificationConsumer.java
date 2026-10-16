package com.property.notification.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.property.notification.domain.NotificationEventType;
import com.property.notification.repository.UserPreferenceRepository;
import com.property.notification.service.ChannelDispatcher;
import com.property.notification.service.EventRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaNotificationConsumer {

    private static final UUID PLACEHOLDER_ORG =
            UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final ObjectMapper objectMapper;
    private final EventRouter router;
    private final ChannelDispatcher dispatcher;
    private final UserPreferenceRepository preferenceRepository;

    @KafkaListener(
            topics = {
                "auth.user.events",
                "lease.lease.created",
                "lease.lease.approved",
                "lease.lease.terminated",
                "maintenance.ticket.created",
                "maintenance.ticket.resolved",
                "property.property.registered",
                "payment.events.received",
                "payment.events.failed",
                "payment.events.initiated",
                "mpesa.events.stk.requested",
                "mpesa.events.stk.result",
                "invoice.invoice.created"
            },
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onEvent(
            String rawPayload,
            @Header(value = KafkaHeaders.RECEIVED_TOPIC, required = false) String topic,
            Acknowledgment ack) {
        try {
            JsonNode node = objectMapper.readTree(rawPayload);
            String rawType = node.path("eventType").asText("");
            NotificationEventType type = NotificationEventType.fromRaw(
                    rawType.isBlank() ? topic : rawType);

            EventRouter.RoutedEvent routed = router.route(type, node);

            // Resolve org: prefer event payload, fall back to recipient's preference,
            // then placeholder.
            UUID orgId = routed.getOrganizationId();
            if (orgId == null && routed.getTenantId() != null) {
                orgId = preferenceRepository.findByPublicId(routed.getTenantId())
                        .map(p -> p.getOrganizationId())
                        .orElse(null);
            }
            if (orgId == null) {
                log.warn("No organizationId for event {} (tenant={}); using placeholder",
                        type, routed.getTenantId());
                orgId = PLACEHOLDER_ORG;
            }

            log.info("Routed event: type={} org={} tenant={} landlord={}",
                    type, orgId, routed.getTenantId(), routed.getLandlordId());

            dispatcher.dispatch(orgId, routed.getTenantId(),
                    type.name(), "RentFlow notification", routed.getSummary(), rawPayload);

            if (routed.getLandlordId() != null
                    && !routed.getLandlordId().equals(routed.getTenantId())) {
                dispatcher.dispatch(orgId, routed.getLandlordId(),
                        type.name(), "RentFlow notification", routed.getSummary(), rawPayload);
            }

            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process notification event: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}