package com.apex.PaymentService.module.PaymentSplit.dto.request;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PaymentSplitUpdateDto {

    private UUID invoiceId;

    @DecimalMin(value = "0.01", message = "Allocated amount must be > 0")
    private BigDecimal allocatedAmount;
}
