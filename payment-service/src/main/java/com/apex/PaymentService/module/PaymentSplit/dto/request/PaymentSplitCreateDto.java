package com.apex.PaymentService.module.PaymentSplit.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PaymentSplitCreateDto {

    @NotNull(message = "Payment ID is required")
    private UUID paymentId;

    @NotNull(message = "Invoice ID is required")
    private UUID invoiceId;

    @NotNull(message = "Allocated amount is required")
    @DecimalMin(value = "0.01", message = "Allocated amount must be > 0")
    private BigDecimal allocatedAmount;
}