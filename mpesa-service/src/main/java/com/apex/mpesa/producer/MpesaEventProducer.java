package com.apex.mpesa.producer;

import com.apex.mpesa.entity.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MpesaEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishStkResult(Transaction tx) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "mpesa.events.stk.result");
        event.put("transactionId", tx.getId());
        event.put("checkoutRequestId", tx.getCheckoutRequestId());
        event.put("tenantId", parseUuid(tx.getTenantId()));
        event.put("leaseId", parseUuid(tx.getLeaseId()));
        event.put("amount", tx.getAmount());
        event.put("status", tx.getStatus() != null ? tx.getStatus().name() : null);
        event.put("mpesaReceipt", tx.getMpesaReceiptNumber());
        event.put("correlationId", UUID.randomUUID());

        // landlordId is not on the transaction — notification engine
        // resolves tenant only for now (add cross-service lookup later)
        kafkaTemplate.send("mpesa.events.stk.result", tx.getId().toString(), event);
        log.info("Published StkResultEvent for {} status={} amount={}",
                tx.getCheckoutRequestId(), tx.getStatus(), tx.getAmount());
    }

    private UUID parseUuid(Object v) {
        if (v == null) return null;
        try {
            return UUID.fromString(v.toString());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
