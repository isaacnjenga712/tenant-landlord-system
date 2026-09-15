package com.apex.PaymentService.module.Payment.event;

import com.apex.PaymentService.module.Payment.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProcessedEvent {
    private UUID paymentId;
    private PaymentStatus status;
    private LocalDateTime processedAt;
}
