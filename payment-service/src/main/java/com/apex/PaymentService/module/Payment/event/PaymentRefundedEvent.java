package com.apex.PaymentService.module.Payment.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRefundedEvent {
    private UUID paymentId;
    private BigDecimal amount;
    private LocalDateTime refundedAt;
}
