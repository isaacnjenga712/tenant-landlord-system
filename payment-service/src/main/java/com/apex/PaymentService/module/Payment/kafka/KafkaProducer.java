package com.apex.PaymentService.module.Payment.kafka;

import com.apex.PaymentService.module.Payment.entity.Payment;
import com.apex.PaymentService.module.Payment.event.PaymentCreatedEvent;
import com.apex.PaymentService.module.Payment.event.PaymentProcessedEvent;
import com.apex.PaymentService.module.Payment.event.PaymentRefundedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPaymentCreatedEvent(Payment payment) {
        // Build event DTO
        PaymentCreatedEvent event = new PaymentCreatedEvent(
                payment.getId(),
                payment.getTenantId(),
                payment.getAmount(),
                payment.getMethod(),
                payment.getStatus()
        );
        kafkaTemplate.send("payment-created", payment.getId().toString(), event);
        log.info("Sent payment-created event for payment {}", payment.getId());
    }

    public void sendPaymentProcessedEvent(Payment payment) {
        PaymentProcessedEvent event = new PaymentProcessedEvent(
                payment.getId(),
                payment.getStatus(),
                payment.getProcessedAt()
        );
        kafkaTemplate.send("payment-processed", payment.getId().toString(), event);
        log.info("Sent payment-processed event for payment {}", payment.getId());
    }

    public void sendPaymentRefundedEvent(Payment payment) {
        PaymentRefundedEvent event = new PaymentRefundedEvent(
                payment.getId(),
                payment.getAmount(),
                payment.getProcessedAt()
        );
        kafkaTemplate.send("payment-refunded", payment.getId().toString(), event);
        log.info("Sent payment-refunded event for payment {}", payment.getId());
    }
}
