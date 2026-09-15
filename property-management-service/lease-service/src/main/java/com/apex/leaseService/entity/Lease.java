package com.apex.leaseService.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "leases")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lease {

    // ========== Primary Key ==========
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    private UUID id;

    // ========== Business Identifiers ==========
    @Column(name = "unit_id")
    private UUID unitId;

    @Column(name = "property_id")
    private UUID propertyId;          // External property ID (UUID)

    @Column(name = "tenant_id")
    private UUID tenantId;            // ✅ UUID (matches your User entity)

    @Column(name = "landlord_id")
    private UUID landlordId;          // ✅ UUID

    // ========== Financials ==========
    @Column(name = "rent_amount", precision = 19, scale = 2)
    private BigDecimal rentAmount;

    @Column(name = "security_deposit_amount", precision = 19, scale = 2)
    private BigDecimal securityDepositAmount;

    // ========== Dates ==========
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    // ========== Status ==========
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaseStatus status;

    // ========== Activation & Termination ==========
    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Column(name = "termination_date")
    private LocalDate terminationDate;

    @Column(name = "termination_reason")
    private String terminationReason;

    @Column(name = "void_reason")
    private String voidReason;

    // ========== Tenants (Many‑to‑Many with User) ==========
    @ManyToMany
    @JoinTable(
        name = "lease_tenants",
        joinColumns = @JoinColumn(name = "lease_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> tenants = new HashSet<>();

    // ========== Auditing ==========
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // ========== Helper methods for compatibility with LeaseCommandService ==========
    public UUID getLeaseId() {
        return id;
    }

    // If your command service uses monthlyRent, map it to rentAmount
    public BigDecimal getMonthlyRent() {
        return rentAmount;
    }

    public void setMonthlyRent(BigDecimal monthlyRent) {
        this.rentAmount = monthlyRent;
    }

    // ========== Lifecycle hooks ==========
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}