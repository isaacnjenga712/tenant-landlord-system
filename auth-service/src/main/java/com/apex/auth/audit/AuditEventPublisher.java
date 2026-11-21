package com.apex.auth.audit;

import com.platform.common.audit.AuditLog;
import com.platform.common.audit.AuditLogRepository;
import com.platform.common.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditEventPublisher {

    private final AuditLogRepository auditLogRepository;

    /**
     * Writes an audit log entry. Called from the auth service on
     * security-relevant events (login, register, role change, etc.).
     *
     * <p>This is fire-and-forget — the caller doesn't wait for the write
     * to complete. If the write fails, it's logged but doesn't break the
     * main flow.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publish(UUID orgId, String eventType, UUID actorId, String resourceType,
            String resourceId, String payload) {
try {
String payloadHash = sha256(payload);

AuditLog entry = AuditLog.builder()
        .orgId(orgId)
        .actorId(actorId)
        .eventType(eventType)
        .resourceType(resourceType)
        .resourceId(resourceId)
        .payloadHash(payloadHash)
        .createdAt(LocalDateTime.now())
        .build();

auditLogRepository.save(entry);

log.debug("Audit event written: {} org={} actor={}",
        eventType, orgId, actorId);
} catch (Exception e) {
log.error("Failed to write audit event {}: {}", eventType, e.getMessage(), e);
}
}

    private String sha256(String input) {
        if (input == null) input = "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            return "hash-error";
        }
    }
}
