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

    @Column(unique = true, nullable = false)
    private String checkoutRequestId;

    private String merchantRequestId;

    private String phoneNumber;

    private BigDecimal amount;    // changed from Double to BigDecimal

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
    }
}