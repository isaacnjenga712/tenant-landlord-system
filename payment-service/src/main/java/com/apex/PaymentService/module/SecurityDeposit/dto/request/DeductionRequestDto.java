package com.apex.PaymentService.module.SecurityDeposit.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DeductionRequestDto {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Deduction must be > 0")
    private BigDecimal amount;

    @NotBlank(message = "Description is required")
    private String description; // can be stored in a separate table or as metadata
}
