package com.apex.PaymentService.module.DepositDeduction.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class DepositDeductionUpdateDto {

    @Size(max = 2000)
    private String description;

    @DecimalMin(value = "0.01", message = "Amount must be > 0")
    private BigDecimal amount;

    private String supportingDocumentUrl;

    private UUID approvedBy;
}
