package com.apex.PaymentService.module.AuditLog.entity;

import com.apex.PaymentService.module.AuditLog.enums.ActionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", length = 20, nullable = false)
    private ActionType actionType;

    @Column(name = "amount_delta", precision = 12, scale = 2)
    private BigDecimal amountDelta;

    @Column(name = "before_trust_balance", precision = 12, scale = 2)
    private BigDecimal beforeTrustBalance;

    @Column(name = "after_trust_balance", precision = 12, scale = 2)
    private BigDecimal afterTrustBalance;

    @Column(name = "before_operational_balance", precision = 12, scale = 2)
    private BigDecimal beforeOperationalBalance;

    @Column(name = "after_operational_balance", precision = 12, scale = 2)
    private BigDecimal afterOperationalBalance;

    private String reason;

    @CreationTimestamp
    @Column(name = "performed_at", updatable = false)
    private LocalDateTime performedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String metadata;  // extra info as JSON
}
