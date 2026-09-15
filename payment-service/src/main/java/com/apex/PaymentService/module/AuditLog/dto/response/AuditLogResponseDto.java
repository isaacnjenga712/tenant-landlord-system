package com.apex.PaymentService.module.AuditLog.dto.response;

import com.apex.PaymentService.module.AuditLog.enums.ActionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AuditLogResponseDto {
    private UUID id;
    private UUID accountId;
    private ActionType actionType;
    private BigDecimal amountDelta;
    private BigDecimal beforeTrustBalance;
    private BigDecimal afterTrustBalance;
    private BigDecimal beforeOperationalBalance;
    private BigDecimal afterOperationalBalance;
    private String reason;
    private LocalDateTime performedAt;
    private String metadata;
}
