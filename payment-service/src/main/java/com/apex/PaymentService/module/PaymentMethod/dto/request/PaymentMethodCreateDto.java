package com.apex.PaymentService.module.PaymentMethod.dto.request;

import com.apex.PaymentService.module.PaymentMethod.enums.PaymentMethodType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class PaymentMethodCreateDto {

    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;

    @NotBlank(message = "Gateway customer ID is required")
    @Size(max = 100)
    private String gatewayCustomerId;

    @NotBlank(message = "Gateway payment method ID is required")
    @Size(max = 100)
    private String gatewayPaymentMethodId;

    @NotNull(message = "Payment method type is required")
    private PaymentMethodType type;

    @Pattern(regexp = "^[0-9]{4}$", message = "Last four must be exactly 4 digits")
    private String lastFour; // optional

    @Min(value = 1, message = "Expiry month must be between 1 and 12")
    @Max(value = 12, message = "Expiry month must be between 1 and 12")
    private Integer expiryMonth; // optional

    @Min(value = 2024, message = "Expiry year must be valid")
    private Integer expiryYear; // optional

    private Boolean isDefault = false;
}