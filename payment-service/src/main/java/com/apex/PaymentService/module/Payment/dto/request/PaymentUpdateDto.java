package com.apex.PaymentService.module.Payment.dto.request;

import com.apex.PaymentService.module.Payment.enums.PaymentStatus;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PaymentUpdateDto {

    private UUID invoiceId;
    private BigDecimal amount;
    private PaymentStatus status;
    private String gatewayTransactionId;
    private String gatewayResponse;
}