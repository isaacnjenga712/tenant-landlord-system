package com.property.notification.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.property.notification.domain.NotificationLog;
import com.property.notification.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaNotificationConsumer {

    private final NotificationLogRepository logRepository;
    private final ObjectMapper objectMapper;

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
    public void onEvent(String rawPayload, Acknowledgment ack) {
        try {
            JsonNode node = objectMapper.readTree(rawPayload);
            String eventType = node.path("eventType").asText("unknown");
            String eventId = node.path("eventId").asText(null);

            log.info("Notification event received: type={} eventId={}", eventType, eventId);

            NotificationLog entry = NotificationLog.builder()
                    .eventType(eventType)
                    .channel(NotificationLog.Channel.IN_APP)
                    .status(NotificationLog.Status.SENT)
                    .payload(rawPayload)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .attempts(0)
                    .build();
            logRepository.save(entry);

            ack.acknowledge();
        } catch (Exception e) {
            log.error("Notification processing failed: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}