package com.apex.leaseService.consumer;

import com.platform.common.constants.KafkaTopics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DltConsumer {

    /**
     * Handles messages that failed processing and landed in Dead Letter Topics.
     * You can save them to a database table for manual inspection and replay.
     */
    @KafkaListener(topics = {
            KafkaTopics.PAYMENT_RECEIVED_DLT,
            KafkaTopics.PAYMENT_FAILED_DLT
    }, groupId = "lease-group-dlt")
    public void handleDlt(
            @Payload String failedMessage,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String originalTopic,
            @Header("tenant_id") String tenantId) {

        log.error("Message landed in DLT from topic: {}. Tenant: {}. Payload: {}",
                originalTopic, tenantId, failedMessage);

        // TODO: Save to a dead-letter table for manual inspection/replay
        // deadLetterRepository.save(new DeadLetterRecord(originalTopic, failedMessage, tenantId));
    }
}
