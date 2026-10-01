package com.property.notification.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.property.notification.domain.NotificationEventType;
import com.property.notification.service.ChannelDispatcher;
import com.property.notification.service.EventRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaNotificationConsumer {

    private final ObjectMapper objectMapper;
    private final EventRouter router;
    private final ChannelDispatcher dispatcher;

    @KafkaListener(
            topics = {
                "auth.user.events",
                "lease.lease.created",
                "lease.lease.terminated",
                "maintenance.ticket.created",
                "maintenance.ticket.resolved",
                "property.property.registered",
                "payment.events.received",
                "payment.events.failed",
                "payment.events.initiated",
                "mpesa.events.stk.requested",
                "mpesa.events.stk.result"
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

            log.info("Routed event: type={} tenant={} landlord={}",
                    type, routed.getTenantId(), routed.getLandlordId());

            // Notify tenant
            dispatcher.dispatch(routed.getTenantId(),
                    type.name(), "RentFlow notification", routed.getSummary(), rawPayload);

            // Notify landlord (if different from tenant)
            if (routed.getLandlordId() != null
                    && !routed.getLandlordId().equals(routed.getTenantId())) {
                dispatcher.dispatch(routed.getLandlordId(),
                        type.name(), "RentFlow notification", routed.getSummary(), rawPayload);
            }

            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process notification event: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}