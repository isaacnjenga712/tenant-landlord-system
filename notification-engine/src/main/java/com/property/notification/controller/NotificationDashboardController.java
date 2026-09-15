package com.property.notification.controller;

import com.property.notification.domain.NotificationLog;
import com.property.notification.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/notifications")
@RequiredArgsConstructor
public class NotificationDashboardController {

    private final NotificationLogRepository logRepository;

    @GetMapping("/logs")
    public List<NotificationLog> getLogs() {
        return logRepository.findAll();
    }

    @PostMapping("/retry/{id}")
    public ResponseEntity<String> retryNotification(@PathVariable String id) {
        NotificationLog log = logRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Log not found with id: " + id));
        // Reset status and attempts, then scheduler will pick up
        log.setStatus(NotificationLog.Status.PENDING);
        log.setAttempts(0);
        log.setUpdatedAt(LocalDateTime.now());
        logRepository.save(log);
        return ResponseEntity.ok("Log marked for retry");
    }
}
