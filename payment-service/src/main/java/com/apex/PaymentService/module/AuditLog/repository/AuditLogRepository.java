package com.apex.PaymentService.module.AuditLog.repository;

import com.apex.PaymentService.module.AuditLog.entity.AuditLog;
import com.apex.PaymentService.module.AuditLog.enums.ActionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID>,
                                            JpaSpecificationExecutor<AuditLog> {

    Page<AuditLog> findByOrgIdAndAccountId(UUID orgId, UUID accountId, Pageable pageable);

    Page<AuditLog> findByOrgIdAndAccountIdAndActionType(UUID orgId, UUID accountId, ActionType actionType, Pageable pageable);
}