package com.apex.leaseService.consumer;

import com.apex.leaseService.producer.LeaseCompensationPublisher;
import com.apex.leaseService.service.LeaseCommandService;
import com.platform.common.constants.KafkaTopics;
import com.platform.common.events.billing.PaymentFailedEvent;
import com.platform.common.events.lease.LeaseCompensationRequiredEvent;
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
public class PaymentFailedConsumer {

    private final LeaseCommandService commandService;
    private final LeaseCompensationPublisher compensationPublisher;

    @KafkaListener(topics = KafkaTopics.PAYMENT_FAILED, groupId = "lease-group")
    public void handlePaymentFailed(
            @Payload PaymentFailedEvent event,
            @Header("tenant_id") String tenantId,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment ack) {

        log.info("Payment failed for lease: {}. Initiating compensation.", event.getLeaseId());

        try {
            // 1. Void the lease locally
            commandService.voidLease(event.getLeaseId(), event.getFailureReason(), tenantId);

            // 2. Publish compensation event for downstream services
            //    We set propertyId to null; you can fetch it from DB if needed.
            LeaseCompensationRequiredEvent compensationEvent = new LeaseCompensationRequiredEvent(
                    event.getLeaseId(),
                    null, // propertyId – can be fetched from LeaseRepository if needed
                    event.getTenantId(),
                    event.getFailureReason(),
                    event.getCorrelationId()
            );
            compensationPublisher.publishCompensation(compensationEvent, tenantId);

            ack.acknowledge();
            log.info("Compensation completed for lease: {}", event.getLeaseId());
        } catch (Exception e) {
            log.error("Error during compensation for lease: {}", event.getLeaseId(), e);
            // We commit the offset anyway to avoid poisoning the topic.
            // You may want to send to a manual DLQ or log for manual intervention.
            ack.acknowledge();
        }
    }
}
