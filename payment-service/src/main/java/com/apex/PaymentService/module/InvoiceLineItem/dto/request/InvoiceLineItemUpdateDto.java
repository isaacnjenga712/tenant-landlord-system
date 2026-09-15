package com.apex.PaymentService.module.InvoiceLineItem.dto.request;

import com.apex.PaymentService.module.InvoiceLineItem.enums.LineItemCategory;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InvoiceLineItemUpdateDto {

    @Size(max = 255)
    private String description;

    private LineItemCategory category;

    @Min(1)
    private Integer quantity;

    @DecimalMin("0.01")
    private BigDecimal unitPrice;

    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private BigDecimal taxRate;
}