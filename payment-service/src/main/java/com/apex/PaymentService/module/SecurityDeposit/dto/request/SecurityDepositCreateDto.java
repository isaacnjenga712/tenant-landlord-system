package com.apex.PaymentService.module.SecurityDeposit.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class SecurityDepositCreateDto {

    @NotNull(message = "Lease ID is required")
    private UUID leaseId;

    @NotNull(message = "Total deposit is required")
    @DecimalMin(value = "0.01", message = "Total deposit must be > 0")
    private BigDecimal totalDeposit;

    private UUID heldInAccountId; // optional
}
