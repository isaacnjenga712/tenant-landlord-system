package com.property.admin.service;

import com.platform.common.dtos.PageResponse;
import com.property.admin.dto.AuditLogDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AdminAuditService {

    /**
     * Placeholder. Wire to payment-service's /api/v1/audit-logs endpoint
     * once an AuditClient is added (same pattern as AuthClient).
     */
    public PageResponse<AuditLogDto> list(int page, int size, String actor, String action) {
        log.debug("Audit log query — page={} size={} actor={} action={}",
                page, size, actor, action);
        return PageResponse.empty(page, size);
    }
}
