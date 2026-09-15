package com.apex.leaseService.producer;

import com.platform.common.constants.KafkaTopics;                           // ✅ Fixed
import com.platform.common.events.lease.LeaseCompensationRequiredEvent;    // ✅ Fixed
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaseCompensationPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishCompensation(LeaseCompensationRequiredEvent event, String tenantId) {
        Message<LeaseCompensationRequiredEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, KafkaTopics.LEASE_COMPENSATION_REQUIRED)
                .setHeader(KafkaHeaders.KEY, event.getPropertyId())
                .setHeader("tenant_id", tenantId)
                .setHeader("correlation_id", event.getCorrelationId())
                .build();
        kafkaTemplate.send(message);
        log.info("Published compensation event for lease: {}", event.getLeaseId());
    }
}
