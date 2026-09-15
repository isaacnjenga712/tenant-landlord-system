package com.apex.PaymentService.module.Payment.dto.response;

import com.apex.PaymentService.module.Payment.enums.PaymentMethod;
import com.apex.PaymentService.module.Payment.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PaymentResponseDto {
    private UUID id;
    private UUID invoiceId;
    private UUID tenantId;
    private BigDecimal amount;
    private PaymentMethod method;
    private String gatewayTransactionId;
    private PaymentStatus status;
    private Boolean appliedToInvoice;
    private String gatewayResponse;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}