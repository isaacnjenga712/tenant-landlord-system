package com.apex.PaymentService.module.PaymentAccount.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PaymentAccountUpdateDto {

    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    private String currency;

    private Boolean isActive;
}
