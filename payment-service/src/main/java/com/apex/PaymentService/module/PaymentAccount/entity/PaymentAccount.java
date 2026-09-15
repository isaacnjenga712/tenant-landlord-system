package com.apex.PaymentService.module.PaymentAccount.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Where;

import com.apex.PaymentService.module.PaymentAccount.enums.EntityType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "payment_accounts",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_entity_type_id",
        columnNames = {"entity_type", "entity_id"}
    )
)
@Check(constraints = "trust_balance >= 0")   // ✅ database check constraint
@Where(clause = "is_active = true")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", length = 20, nullable = false)
    private EntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(name = "trust_balance", precision = 12, scale = 2, nullable = false)
    private BigDecimal trustBalance;

    @Column(name = "operational_balance", precision = 12, scale = 2, nullable = false)
    private BigDecimal operationalBalance;

    @Column(length = 3, nullable = false)
    private String currency;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_active")
    private Boolean isActive;

    @Version
    @Column(name = "version")
    private Long version;

    // --- Business logic methods ---
    public void addToTrust(BigDecimal amount) {
        if (this.trustBalance.add(amount).compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Insufficient trust balance");
        }
        this.trustBalance = this.trustBalance.add(amount);
    }

    public void addToOperational(BigDecimal amount) {
        this.operationalBalance = this.operationalBalance.add(amount);
    }
}
