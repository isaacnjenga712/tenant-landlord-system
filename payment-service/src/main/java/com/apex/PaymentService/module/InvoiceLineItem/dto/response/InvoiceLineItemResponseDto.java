package com.apex.PaymentService.module.InvoiceLineItem.dto.response;

import com.apex.PaymentService.module.InvoiceLineItem.enums.LineItemCategory;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class InvoiceLineItemResponseDto {
    private UUID id;
    private UUID invoiceId;
    private String description;
    private LineItemCategory category;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal total;
    private BigDecimal taxRate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}