package com.apex.PaymentService.module.Invoice.producer;

import com.apex.PaymentService.module.Invoice.dto.response.InvoiceResponseDto;
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
public class InvoiceEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishInvoiceCreated(InvoiceResponseDto invoice,
                                      UUID tenantId,
                                      UUID landlordId) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "invoice.invoice.created");
        event.put("invoiceId", invoice.getId());
        event.put("invoiceNumber", invoice.getInvoiceNumber());
        event.put("leaseId", invoice.getLeaseId());
        event.put("tenantId", tenantId);
        event.put("landlordId", landlordId);
        event.put("totalAmount", invoice.getTotalAmount());
        event.put("dueDate", invoice.getDueDate());
        event.put("correlationId", UUID.randomUUID());

        kafkaTemplate.send("invoice.invoice.created", invoice.getId().toString(), event);
        log.info("Published InvoiceCreatedEvent for {} (tenant={} landlord={})",
                invoice.getInvoiceNumber(), tenantId, landlordId);
    }
}
