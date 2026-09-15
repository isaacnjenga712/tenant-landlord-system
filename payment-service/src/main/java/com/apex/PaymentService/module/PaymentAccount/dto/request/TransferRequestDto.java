package com.apex.PaymentService.module.PaymentAccount.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequestDto {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be > 0")
    private BigDecimal amount;

    @NotBlank(message = "Source balance is required")
    private String fromBalance;  // "trust" or "operational"

    @NotBlank(message = "Destination balance is required")
    private String toBalance;    // "trust" or "operational"

    private String reason;
}