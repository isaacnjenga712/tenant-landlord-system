package com.apex.PaymentService.module.PaymentSplit.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PaymentSplitResponseDto {
    private UUID id;
    private UUID paymentId;
    private UUID invoiceId;
    private BigDecimal allocatedAmount;
    private LocalDateTime createdAt;
}
