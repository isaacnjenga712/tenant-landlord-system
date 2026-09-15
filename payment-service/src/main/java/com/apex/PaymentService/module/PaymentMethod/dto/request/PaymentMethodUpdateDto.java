package com.apex.PaymentService.module.PaymentMethod.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PaymentMethodUpdateDto {

    private Boolean isDefault;

    private Boolean isActive;

    @Pattern(regexp = "^[0-9]{4}$", message = "Last four must be exactly 4 digits")
    private String lastFour;

    @Min(1) @Max(12)
    private Integer expiryMonth;

    @Min(2024)
    private Integer expiryYear;
}
