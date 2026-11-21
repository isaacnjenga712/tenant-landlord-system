package com.platform.common.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogCleanupJob {

    private final AuditLogRepository auditLogRepository;
    private final TenantRetentionPolicy retentionPolicy;

    /**
     * Runs nightly at 2 AM. Deletes audit logs older than the
     * per-tenant retention period.
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupExpiredAuditLogs() {
        log.info("Starting audit log cleanup");

        List<UUID> orgIds = auditLogRepository.findDistinctOrgIds();

        int totalDeleted = 0;

        for (UUID orgId : orgIds) {
            LocalDateTime cutoff = retentionPolicy.getCutoff(orgId);
            int deleted = auditLogRepository.deleteOldByOrg(orgId, cutoff);
            totalDeleted += deleted;
            log.info("Deleted {} audit logs for org {} (cutoff {})", deleted, orgId, cutoff);
        }

        log.info("Audit log cleanup complete: {} total deleted across {} orgs",
                totalDeleted, orgIds.size());
    }
}