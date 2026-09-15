package com.apex.PropertyManagementService.producer;

import com.platform.common.constants.KafkaTopics;
import com.platform.common.events.property.PropertyRegisteredEvent;
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
public class PropertyEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPropertyRegistered(PropertyRegisteredEvent event, UUID tenantId) {
        String tenantIdStr = tenantId != null ? tenantId.toString() : null;
        Message<PropertyRegisteredEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, KafkaTopics.PROPERTY_REGISTERED)
                .setHeader(KafkaHeaders.KEY, event.getPropertyId().toString())   // UUID → String
                .setHeader("tenant_id", tenantIdStr)
                .setHeader("correlation_id", event.getCorrelationId().toString())
                .build();
        kafkaTemplate.send(message);
        log.info("Published PropertyRegisteredEvent for property: {}", event.getPropertyId());
    }
}
