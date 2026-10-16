package com.property.notification.controller;

import com.property.notification.domain.NotificationLog;
import com.property.notification.repository.NotificationLogRepository;
import com.platform.common.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationDashboardController {

    private final NotificationLogRepository logRepository;

    @GetMapping("/logs")
    public Page<NotificationLog> getLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID orgId = TenantContext.require();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return logRepository.findByOrganizationId(orgId, pageable);
    }

    @GetMapping("/logs/{id}")
    public ResponseEntity<NotificationLog> getLog(@PathVariable String id) {
        UUID orgId = TenantContext.require();
        return logRepository.findById(id)
                .filter(l -> orgId.equals(l.getOrganizationId()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/retry/{id}")
    public ResponseEntity<String> retryNotification(@PathVariable String id) {
        UUID orgId = TenantContext.require();
        NotificationLog log = logRepository.findById(id)
                .filter(l -> orgId.equals(l.getOrganizationId()))
                .orElseThrow(() -> new IllegalArgumentException("Log not found with id: " + id));
        log.setStatus(NotificationLog.Status.PENDING);
        log.setAttempts(0);
        log.setUpdatedAt(LocalDateTime.now());
        logRepository.save(log);
        return ResponseEntity.ok("Log marked for retry");
    }
}