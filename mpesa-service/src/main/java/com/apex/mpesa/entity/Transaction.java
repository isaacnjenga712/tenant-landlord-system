package com.apex.mpesa.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    private String id;

    /** SaaS tenant (org) — stored as String to match existing columns. */
    @Column(name = "org_id", nullable = false, updatable = false, length = 36)
    private String orgId;

    @Column(unique = true, nullable = false)
    private String checkoutRequestId;

    private String merchantRequestId;

    private String phoneNumber;

    private BigDecimal amount;

    private String mpesaReceiptNumber;

    private String resultDescription;

    private Integer resultCode;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    private String accountReference;

    @Column(name = "tenant_id")
    private String tenantId;

    @Column(name = "lease_id")
    private String leaseId;

    @Column(name = "invoice_id")
    private String invoiceId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.orgId == null) {
            throw new IllegalStateException("org_id is required — set TenantContext before saving");
        }
    }
}