package com.property.notification.controller;

import com.property.notification.domain.NotificationLog;
import com.property.notification.repository.NotificationLogRepository;
import com.platform.common.dtos.PageResponse;
import com.platform.common.dtos.admin.NotificationLogDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneOffset;

@RestController
@RequestMapping("/api/v1/internal/notifications")
@RequiredArgsConstructor
public class InternalNotificationController {

    private final NotificationLogRepository logRepo;

    @GetMapping("/logs")
    public PageResponse<NotificationLogDto> logs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) String status) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<NotificationLog> result = logRepo.findAll(pageable);

        return new PageResponse<>(
                result.getContent().stream().map(this::toDto).toList(),
                result.getTotalElements(),
                result.getNumber(),
                result.getSize(),
                result.hasNext()
        );
    }

    @PostMapping("/retry/{id}")
    public void retry(@PathVariable String id) {
        throw new UnsupportedOperationException("Retry not implemented");
    }

    private NotificationLogDto toDto(NotificationLog l) {
        return new NotificationLogDto(
                l.getId(),
                null,                              // recipientPublicId — add to entity if you track it
                l.getEventType(),
                l.getChannel() != null ? l.getChannel().name() : null,
                l.getStatus() != null ? l.getStatus().name() : null,
                l.getAttempts(),
                l.getErrorMessage(),
                l.getCreatedAt() != null
                        ? l.getCreatedAt().toInstant(ZoneOffset.UTC)
                        : null
        );
    }
}
