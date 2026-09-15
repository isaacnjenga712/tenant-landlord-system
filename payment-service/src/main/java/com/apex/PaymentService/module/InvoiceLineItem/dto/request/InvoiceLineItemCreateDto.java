package com.apex.PaymentService.module.InvoiceLineItem.dto.request;

import com.apex.PaymentService.module.InvoiceLineItem.enums.LineItemCategory;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class InvoiceLineItemCreateDto {

    @NotNull(message = "Invoice ID is required")
    private UUID invoiceId;

    @NotBlank(message = "Description is required")
    @Size(max = 255)
    private String description;

    @NotNull(message = "Category is required")
    private LineItemCategory category;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity = 1;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.01", message = "Unit price must be > 0")
    private BigDecimal unitPrice;

    @DecimalMin(value = "0.0", message = "Tax rate must be >= 0")
    @DecimalMax(value = "100.0", message = "Tax rate must be <= 100")
    private BigDecimal taxRate = BigDecimal.ZERO;
}
