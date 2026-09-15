package com.apex.PaymentService.module.AuditLog.service.impl;

import com.apex.PaymentService.module.AuditLog.dto.request.AuditLogFilterDto;
import com.apex.PaymentService.module.AuditLog.dto.response.AuditLogResponseDto;
import com.apex.PaymentService.module.AuditLog.entity.AuditLog;
import com.apex.PaymentService.module.AuditLog.enums.ActionType;
import com.apex.PaymentService.module.AuditLog.mapper.AuditLogMapper;
import com.apex.PaymentService.module.AuditLog.repository.AuditLogRepository;
import com.apex.PaymentService.module.AuditLog.service.AuditLogService;
import com.apex.PaymentService.module.AuditLog.spec.AuditLogSpecifications;
import com.apex.PaymentService.module.PaymentAccount.entity.PaymentAccount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository repository;
    private final AuditLogMapper mapper;

    @Async
    @Override
    public void logCreate(PaymentAccount account) {
        AuditLog logEntry = AuditLog.builder()
                .accountId(account.getId())
                .actionType(ActionType.CREATE)
                .afterTrustBalance(account.getTrustBalance())
                .afterOperationalBalance(account.getOperationalBalance())
                .reason("Account created")
                .build();
        repository.save(logEntry);
        log.debug("Audit log saved for CREATE on account {}", account.getId());
    }

    @Async
    @Override
    public void logUpdate(PaymentAccount account, PaymentAccount oldState) {
        AuditLog logEntry = AuditLog.builder()
                .accountId(account.getId())
                .actionType(ActionType.UPDATE)
                .beforeTrustBalance(oldState.getTrustBalance())
                .afterTrustBalance(account.getTrustBalance())
                .beforeOperationalBalance(oldState.getOperationalBalance())
                .afterOperationalBalance(account.getOperationalBalance())
                .reason("Account updated")
                .metadata(String.format("{\"oldCurrency\":\"%s\",\"newCurrency\":\"%s\",\"oldActive\":%b,\"newActive\":%b}",
                        oldState.getCurrency(), account.getCurrency(),
                        oldState.getIsActive(), account.getIsActive()))
                .build();
        repository.save(logEntry);
        log.debug("Audit log saved for UPDATE on account {}", account.getId());
    }

    @Async
    @Override
    public void logDelete(PaymentAccount account) {
        AuditLog logEntry = AuditLog.builder()
                .accountId(account.getId())
                .actionType(ActionType.DELETE)
                .beforeTrustBalance(account.getTrustBalance())
                .afterTrustBalance(BigDecimal.ZERO)
                .beforeOperationalBalance(account.getOperationalBalance())
                .afterOperationalBalance(BigDecimal.ZERO)
                .reason("Account soft-deleted")
                .build();
        repository.save(logEntry);
        log.debug("Audit log saved for DELETE on account {}", account.getId());
    }

    @Async
    @Override
    public void logAdjustment(PaymentAccount account, BigDecimal delta, String target, String reason) {
        // Compute before values: before = after - delta
        BigDecimal beforeTrust = account.getTrustBalance();
        BigDecimal beforeOp = account.getOperationalBalance();
        if ("trust".equalsIgnoreCase(target)) {
            beforeTrust = account.getTrustBalance().subtract(delta);
        } else {
            beforeOp = account.getOperationalBalance().subtract(delta);
        }

        AuditLog logEntry = AuditLog.builder()
                .accountId(account.getId())
                .actionType(ActionType.ADJUST)
                .amountDelta(delta)
                .beforeTrustBalance(beforeTrust)
                .afterTrustBalance(account.getTrustBalance())
                .beforeOperationalBalance(beforeOp)
                .afterOperationalBalance(account.getOperationalBalance())
                .reason(reason)
                .metadata(String.format("{\"target\":\"%s\"}", target))
                .build();
        repository.save(logEntry);
        log.debug("Audit log saved for ADJUST on account {}: {} {}", account.getId(), delta, target);
    }

    @Async
    @Override
    public void logTransfer(PaymentAccount account, BigDecimal amount, String from, String to, String reason) {
        // Calculate before values based on transfer direction
        BigDecimal beforeTrust = account.getTrustBalance();
        BigDecimal beforeOp = account.getOperationalBalance();

        if ("trust".equalsIgnoreCase(from)) {
            beforeTrust = beforeTrust.add(amount); // because after = before - amount
        } else {
            beforeOp = beforeOp.add(amount);
        }

        AuditLog logEntry = AuditLog.builder()
                .accountId(account.getId())
                .actionType(ActionType.TRANSFER)
                .amountDelta(amount)
                .beforeTrustBalance(beforeTrust)
                .afterTrustBalance(account.getTrustBalance())
                .beforeOperationalBalance(beforeOp)
                .afterOperationalBalance(account.getOperationalBalance())
                .reason(reason)
                .metadata(String.format("{\"from\":\"%s\",\"to\":\"%s\"}", from, to))
                .build();
        repository.save(logEntry);
        log.debug("Audit log saved for TRANSFER on account {}: {} from {} to {}", account.getId(), amount, from, to);
    }

    @Override
    public Page<AuditLogResponseDto> getAuditLogs(UUID accountId, Pageable pageable) {
        return repository.findByAccountId(accountId, pageable).map(mapper::toResponseDto);
    }

    @Override
    public Page<AuditLogResponseDto> getAuditLogsWithFilters(AuditLogFilterDto filter, Pageable pageable) {
        Specification<AuditLog> spec = Specification
                .where(AuditLogSpecifications.accountIdEquals(filter.getAccountId()))
                .and(AuditLogSpecifications.actionTypeEquals(filter.getActionType()))
                .and(AuditLogSpecifications.performedAtAfter(filter.getFromDate()))
                .and(AuditLogSpecifications.performedAtBefore(filter.getToDate()));

        return repository.findAll(spec, pageable).map(mapper::toResponseDto);
    }
}
