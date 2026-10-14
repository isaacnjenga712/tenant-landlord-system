package com.apex.PaymentService.module.Invoice.entity;

import com.apex.PaymentService.module.Invoice.enums.InvoiceStatus;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "invoices")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "org_id", columnDefinition = "UUID", nullable = false, updatable = false)
    private UUID orgId;

    @Column(name = "lease_id", nullable = false)
    private UUID leaseId;

    @Column(name = "invoice_number", length = 50, nullable = false)
    private String invoiceNumber;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "total_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "paid_amount", precision = 12, scale = 2)
    private BigDecimal paidAmount;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private InvoiceStatus status;

    @Column(name = "late_fee_applied")
    private Boolean lateFeeApplied;

    @Column(name = "grace_period_days")
    private Integer gracePeriodDays;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String metadata;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @PrePersist
    protected void guardOrg() {
        if (orgId == null) {
            throw new IllegalStateException("org_id is required — set TenantContext before saving");
        }
    }

    public void updateStatus() {
        if (this.paidAmount == null) this.paidAmount = BigDecimal.ZERO;
        if (this.totalAmount == null) this.totalAmount = BigDecimal.ZERO;

        if (this.paidAmount.compareTo(this.totalAmount) >= 0) {
            this.status = InvoiceStatus.paid;
        } else if (this.paidAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.status = InvoiceStatus.partial;
        } else if (this.dueDate != null && this.gracePeriodDays != null) {
            if (LocalDate.now().isAfter(this.dueDate.plusDays(this.gracePeriodDays))) {
                this.status = InvoiceStatus.overdue;
            } else {
                this.status = InvoiceStatus.pending;
            }
        } else {
            this.status = InvoiceStatus.pending;
        }
    }

    public void applyPayment(BigDecimal amount) {
        if (this.status == InvoiceStatus.voided) {
            throw new IllegalStateException("Cannot apply payment to a voided invoice");
        }
        if (this.status == InvoiceStatus.paid) {
            throw new IllegalStateException("Invoice is already fully paid");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }

        BigDecimal outstanding = this.totalAmount.subtract(this.paidAmount);
        if (amount.compareTo(outstanding) > 0) {
            throw new IllegalStateException("Payment amount exceeds outstanding balance");
        }

        this.paidAmount = this.paidAmount.add(amount);
        updateStatus();
    }

    public void reversePayment(BigDecimal amount) {
        if (this.status == InvoiceStatus.voided) {
            throw new IllegalStateException("Cannot reverse payment on a voided invoice");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Reverse amount must be positive");
        }
        if (amount.compareTo(this.paidAmount) > 0) {
            throw new IllegalStateException("Cannot reverse more than the paid amount");
        }

        this.paidAmount = this.paidAmount.subtract(amount);
        updateStatus();
    }
}