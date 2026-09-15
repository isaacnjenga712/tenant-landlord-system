package com.apex.PaymentService.module.Invoice.dto.response;

import com.apex.PaymentService.module.Invoice.enums.InvoiceStatus;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class InvoiceResponseDto {
    private UUID id;
    private UUID leaseId;
    private String invoiceNumber;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDate dueDate;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private InvoiceStatus status;
    private Boolean lateFeeApplied;
    private Integer gracePeriodDays;
    private String metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
