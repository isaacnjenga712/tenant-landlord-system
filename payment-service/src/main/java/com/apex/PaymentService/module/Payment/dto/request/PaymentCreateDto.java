package com.apex.PaymentService.module.Payment.dto.request;

import com.apex.PaymentService.module.Payment.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PaymentCreateDto {

    private UUID invoiceId; // optional

    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be > 0")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod method;

    @Size(max = 100)
    private String gatewayTransactionId;

    private String gatewayResponse;
}
