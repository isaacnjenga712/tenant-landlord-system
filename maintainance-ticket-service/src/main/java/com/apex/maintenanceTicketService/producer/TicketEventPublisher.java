package com.apex.maintenanceTicketService.producer;

import com.platform.common.constants.KafkaTopics;
import com.platform.common.events.maintenance.TicketCreatedEvent;
import com.platform.common.events.maintenance.TicketResolvedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishTicketCreated(TicketCreatedEvent event, UUID tenantId) {
        String tenantIdStr = tenantId != null ? tenantId.toString() : null;
        Message<TicketCreatedEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, KafkaTopics.TICKET_CREATED)
                .setHeader(KafkaHeaders.KEY, event.getTicketId())
                .setHeader("tenant_id", tenantIdStr)
                .setHeader("correlation_id", UUID.randomUUID().toString())
                .build();
        kafkaTemplate.send(message);
        log.info("Published TicketCreatedEvent for ticket: {}", event.getTicketId());
    }

    public void publishTicketResolved(TicketResolvedEvent event, UUID tenantId) {
        String tenantIdStr = tenantId != null ? tenantId.toString() : null;
        Message<TicketResolvedEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, KafkaTopics.TICKET_RESOLVED)
                .setHeader(KafkaHeaders.KEY, event.getTicketId())
                .setHeader("tenant_id", tenantIdStr)
                .setHeader("correlation_id", UUID.randomUUID().toString())
                .build();
        kafkaTemplate.send(message);
        log.info("Published TicketResolvedEvent for ticket: {}", event.getTicketId());
    }
}
