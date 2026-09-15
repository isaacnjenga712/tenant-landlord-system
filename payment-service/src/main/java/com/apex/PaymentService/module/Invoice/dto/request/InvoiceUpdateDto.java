package com.apex.PaymentService.module.Invoice.dto.request;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;

@Data
public class InvoiceUpdateDto {
    private LocalDate dueDate;
    @DecimalMin("0.0") private BigDecimal totalAmount;
    @DecimalMin("0.0") private BigDecimal paidAmount;
    private Integer gracePeriodDays;
    private String status; // only "void" allowed
    private String metadata;
}
