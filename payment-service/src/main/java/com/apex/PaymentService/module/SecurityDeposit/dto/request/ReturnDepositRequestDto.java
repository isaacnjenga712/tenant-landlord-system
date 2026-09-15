package com.apex.PaymentService.module.SecurityDeposit.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ReturnDepositRequestDto {

    @NotNull(message = "Return amount is required")
    @DecimalMin(value = "0.0", message = "Return amount must be >= 0")
    private BigDecimal returnAmount;

    private LocalDate returnedDate; // optional, defaults to today
}
