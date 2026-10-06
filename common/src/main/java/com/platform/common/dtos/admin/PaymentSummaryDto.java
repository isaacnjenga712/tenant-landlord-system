package com.platform.common.dtos.admin;

import java.io.Serializable;
import java.math.BigDecimal;

public record PaymentSummaryDto(
        BigDecimal totalCollected,
        BigDecimal totalPending,
        BigDecimal totalFailed,
        long paymentCount,
        long failedCount
) implements Serializable {

    private static final long serialVersionUID = 1L;

    public static PaymentSummaryDto empty() {
        return new PaymentSummaryDto(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0L, 0L);
    }
}
