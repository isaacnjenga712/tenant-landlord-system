package com.apex.PaymentService.module.Invoice.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class InvoiceCreateDto {
    @NotNull private UUID leaseId;
    @NotBlank @Size(max=50) private String invoiceNumber;
    @NotNull private LocalDate periodStart;
    @NotNull private LocalDate periodEnd;
    @NotNull private LocalDate dueDate;
    @NotNull @DecimalMin("0.01") private BigDecimal totalAmount;
    @DecimalMin("0.0") private BigDecimal paidAmount = BigDecimal.ZERO;
    @Min(0) private Integer gracePeriodDays = 3;
    private String metadata;
}
