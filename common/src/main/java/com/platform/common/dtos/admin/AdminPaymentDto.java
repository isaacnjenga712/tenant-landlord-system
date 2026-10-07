package com.platform.common.dtos.admin;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public record AdminPaymentDto(
        String id,
        String invoiceId,
        String tenantId,
        BigDecimal amount,
        String method,
        String gatewayTransactionId,
        String status,
        Boolean appliedToInvoice,
        Instant processedAt,
        Instant createdAt
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
