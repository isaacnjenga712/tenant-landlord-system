package com.apex.PaymentService.module.Payment.entity;

import com.apex.PaymentService.module.Payment.enums.PaymentMethod;
import com.apex.PaymentService.module.Payment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "invoice_id")
    private UUID invoiceId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private PaymentMethod method;

    @Column(name = "gateway_transaction_id", length = 100)
    private String gatewayTransactionId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentStatus status;

    @Column(name = "applied_to_invoice")
    private Boolean appliedToInvoice;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String gatewayResponse;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ---------- Business Logic Methods ----------

    /**
     * Marks the payment as successful.
     * Validates that the payment is in a state that can be marked successful
     * (i.e., not already success, refunded, or failed permanently).
     */
    public void markSuccess() {
        if (this.status == PaymentStatus.success) {
            throw new IllegalStateException("Payment is already successful");
        }
        if (this.status == PaymentStatus.refunded) {
            throw new IllegalStateException("Cannot mark a refunded payment as successful");
        }
        if (this.status == PaymentStatus.failed) {
            throw new IllegalStateException("Cannot mark a failed payment as successful");
        }
        this.status = PaymentStatus.success;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * Marks the payment as failed.
     * Accepts an optional error response to store in the gateway_response field.
     */
    public void markFailed(String errorResponse) {
        if (this.status == PaymentStatus.success) {
            throw new IllegalStateException("Cannot mark a successful payment as failed");
        }
        if (this.status == PaymentStatus.refunded) {
            throw new IllegalStateException("Cannot mark a refunded payment as failed");
        }
        this.status = PaymentStatus.failed;
        this.processedAt = LocalDateTime.now();
        this.gatewayResponse = errorResponse;
    }

    /**
     * Marks the payment as refunded.
     * Only a successful payment can be refunded.
     */
    public void markRefunded() {
        if (this.status != PaymentStatus.success) {
            throw new IllegalStateException("Only successful payments can be refunded");
        }
        this.status = PaymentStatus.refunded;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * Applies the payment to its linked invoice.
     * Validates that the payment is in 'success' status and not already applied.
     */
    public void applyToInvoice() {
        if (this.status != PaymentStatus.success) {
            throw new IllegalStateException("Only successful payments can be applied to an invoice");
        }
        if (Boolean.TRUE.equals(this.appliedToInvoice)) {
            throw new IllegalStateException("Payment has already been applied to the invoice");
        }
        this.appliedToInvoice = true;
    }

    /**
     * Checks if the payment is in a state that allows update (e.g., not refunded).
     */
    public boolean isUpdatable() {
        return this.status != PaymentStatus.refunded;
    }

    /**
     * Checks if the payment can be deleted (only initiated or failed status).
     */
    public boolean isDeletable() {
        return this.status == PaymentStatus.initiated || this.status == PaymentStatus.failed;
    }

    /**
     * Checks if the payment is already applied to an invoice.
     */
    public boolean isAppliedToInvoice() {
        return Boolean.TRUE.equals(this.appliedToInvoice);
    }
}
