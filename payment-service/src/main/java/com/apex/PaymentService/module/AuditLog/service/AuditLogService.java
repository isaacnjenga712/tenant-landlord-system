package com.apex.PaymentService.module.AuditLog.service;

import com.apex.PaymentService.module.AuditLog.dto.request.AuditLogFilterDto;
import com.apex.PaymentService.module.AuditLog.dto.response.AuditLogResponseDto;
import com.apex.PaymentService.module.AuditLog.entity.AuditLog;
import com.apex.PaymentService.module.PaymentAccount.entity.PaymentAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

public interface AuditLogService {

    // --- Internal logging methods (called by other services) ---
    void logCreate(PaymentAccount account);
    void logUpdate(PaymentAccount account, PaymentAccount oldState);
    void logDelete(PaymentAccount account);
    void logAdjustment(PaymentAccount account, BigDecimal delta, String target, String reason);
    void logTransfer(PaymentAccount account, BigDecimal amount, String from, String to, String reason);

    // --- Query methods ---
    Page<AuditLogResponseDto> getAuditLogs(UUID accountId, Pageable pageable);
    Page<AuditLogResponseDto> getAuditLogsWithFilters(AuditLogFilterDto filter, Pageable pageable);
}
