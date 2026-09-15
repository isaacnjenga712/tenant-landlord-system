package com.apex.leaseService.consumer;

import com.apex.leaseService.service.LeaseCommandService;
import com.platform.common.constants.KafkaTopics;
import com.platform.common.events.billing.PaymentReceivedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentReceivedConsumer {

    private final LeaseCommandService commandService;

    @KafkaListener(topics = KafkaTopics.PAYMENT_RECEIVED, groupId = "lease-group")
    public void handlePaymentReceived(
            @Payload PaymentReceivedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header("tenant_id") String tenantId,
            Acknowledgment ack) {

        log.info("Payment received for lease: {}", event.getLeaseId());
        try {
            commandService.activateLease(event.getLeaseId());
            ack.acknowledge();
            log.debug("Payment event processed successfully for lease: {}", event.getLeaseId());
        } catch (Exception e) {
            log.error("Error processing payment for lease: {}", event.getLeaseId(), e);
            // In case of failure, we still acknowledge the message to avoid infinite retries.
            // You can implement custom retry logic here or send to a manual DLQ.
            ack.acknowledge();
        }
    }
}