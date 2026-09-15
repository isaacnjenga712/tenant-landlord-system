package com.apex.PaymentService.module.InvoiceLineItem.entity;

import com.apex.PaymentService.module.InvoiceLineItem.enums.LineItemCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "invoice_line_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceLineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "invoice_id", nullable = false)
    private UUID invoiceId;

    @Column(length = 255, nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private LineItemCategory category;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", precision = 12, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal total;

    @Column(name = "tax_rate", precision = 5, scale = 2)
    private BigDecimal taxRate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Calculates the total amount: (unitPrice * quantity) + tax.
     * Tax is calculated as a percentage of the subtotal.
     */
    public void calculateTotal() {
        if (this.quantity == null) this.quantity = 1;
        if (this.unitPrice == null) this.unitPrice = BigDecimal.ZERO;
        if (this.taxRate == null) this.taxRate = BigDecimal.ZERO;

        BigDecimal subtotal = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
        BigDecimal tax = subtotal.multiply(this.taxRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        this.total = subtotal.add(tax);
    }
}