package com.apex.PaymentService.module.PaymentAccount.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BalanceAdjustmentDto {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "-999999999.99", message = "Amount is too small")
    private BigDecimal amount;  // positive = add, negative = deduct

    @NotBlank(message = "Target balance is required")
    private String targetBalance;  // "trust" or "operational"

    private String reason;
}
