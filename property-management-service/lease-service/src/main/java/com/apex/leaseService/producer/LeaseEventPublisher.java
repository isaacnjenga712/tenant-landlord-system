package com.apex.leaseService.producer;

import com.platform.common.constants.KafkaTopics;                     // ✅ Fixed
import com.platform.common.events.lease.LeaseCreatedEvent;           // ✅ Fixed
import com.platform.common.events.lease.LeaseTerminatedEvent;        // ✅ Fixed
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
public class LeaseEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishLeaseCreated(LeaseCreatedEvent event, String tenantId) {
        Message<LeaseCreatedEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, KafkaTopics.LEASE_CREATED)
                .setHeader(KafkaHeaders.KEY, event.getPropertyId())
                .setHeader("tenant_id", tenantId)
                .setHeader("correlation_id", event.getCorrelationId())
                .build();
        kafkaTemplate.send(message);
        log.info("Published LeaseCreatedEvent for lease: {}", event.getLeaseId());
    }

    public void publishLeaseTerminated(LeaseTerminatedEvent event, String tenantId) {
        Message<LeaseTerminatedEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, KafkaTopics.LEASE_TERMINATED)
                .setHeader(KafkaHeaders.KEY, event.getPropertyId())
                .setHeader("tenant_id", tenantId)
                .setHeader("correlation_id", UUID.randomUUID().toString())
                .build();
        kafkaTemplate.send(message);
        log.info("Published LeaseTerminatedEvent for lease: {}", event.getLeaseId());
    }
}