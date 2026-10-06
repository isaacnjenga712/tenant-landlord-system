package com.property.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FinanceSummaryDto(
        LocalDate from,
        LocalDate to,
        BigDecimal revenue,
        BigDecimal refunds,
        BigDecimal pending,
        long transactionCount,
        long failedCount
) {}
