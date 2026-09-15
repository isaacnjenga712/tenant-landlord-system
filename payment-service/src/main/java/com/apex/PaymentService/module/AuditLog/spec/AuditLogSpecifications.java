package com.apex.PaymentService.module.AuditLog.spec;

import com.apex.PaymentService.module.AuditLog.entity.AuditLog;
import com.apex.PaymentService.module.AuditLog.enums.ActionType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.UUID;

public class AuditLogSpecifications {

    public static Specification<AuditLog> accountIdEquals(UUID accountId) {
        return (root, query, cb) ->
                accountId == null ? null : cb.equal(root.get("accountId"), accountId);
    }

    public static Specification<AuditLog> actionTypeEquals(ActionType actionType) {
        return (root, query, cb) ->
                actionType == null ? null : cb.equal(root.get("actionType"), actionType);
    }

    public static Specification<AuditLog> performedAtAfter(LocalDateTime fromDate) {
        return (root, query, cb) ->
                fromDate == null ? null : cb.greaterThanOrEqualTo(root.get("performedAt"), fromDate);
    }

    public static Specification<AuditLog> performedAtBefore(LocalDateTime toDate) {
        return (root, query, cb) ->
                toDate == null ? null : cb.lessThanOrEqualTo(root.get("performedAt"), toDate);
    }
}
