package com.apex.PaymentService.module.Payment.event;

import com.apex.PaymentService.module.Payment.enums.PaymentMethod;
import com.apex.PaymentService.module.Payment.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreatedEvent {
    private UUID paymentId;
    private UUID tenantId;
    private BigDecimal amount;
    private PaymentMethod method;
    private PaymentStatus status;
}
