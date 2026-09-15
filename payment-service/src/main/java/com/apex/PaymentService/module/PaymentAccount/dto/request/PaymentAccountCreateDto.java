package com.apex.PaymentService.module.PaymentAccount.dto.request;

import com.apex.PaymentService.module.PaymentAccount.enums.EntityType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PaymentAccountCreateDto {

    @NotNull(message = "Entity type is required")
    private EntityType entityType;

    @NotNull(message = "Entity ID is required")
    private UUID entityId;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    private String currency;

    @DecimalMin(value = "0.0", message = "Trust balance cannot be negative")
    private BigDecimal trustBalance = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Operational balance cannot be negative")
    private BigDecimal operationalBalance = BigDecimal.ZERO;
}
