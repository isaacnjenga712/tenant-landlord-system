package com.apex.PaymentService.module.DepositDeduction.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class DepositDeductionCreateDto {

    @NotNull(message = "Deposit ID is required")
    private UUID depositId;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be > 0")
    private BigDecimal amount;

    private String supportingDocumentUrl;

    private UUID approvedBy; 
}