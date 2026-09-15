package com.apex.PaymentService.module.AuditLog.dto.request;

import com.apex.PaymentService.module.AuditLog.enums.ActionType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AuditLogFilterDto {
    private UUID accountId;
    private ActionType actionType;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
}
