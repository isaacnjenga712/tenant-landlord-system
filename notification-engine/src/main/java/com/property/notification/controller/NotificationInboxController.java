package com.property.notification.controller;

import com.property.notification.dto.NotificationDto;
import com.property.notification.dto.PageResponse;
import com.property.notification.service.NotificationInboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications/inbox")
@RequiredArgsConstructor
public class NotificationInboxController {

    private static final String USER_HEADER = "X-User-PublicId";
    private final NotificationInboxService inboxService;

    @GetMapping
    public PageResponse<NotificationDto> list(
            @RequestHeader(USER_HEADER) UUID userPublicId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return inboxService.list(userPublicId, page, size);
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(@RequestHeader(USER_HEADER) UUID userPublicId) {
        return Map.of("count", inboxService.unreadCount(userPublicId));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@RequestHeader(USER_HEADER) UUID userPublicId,
                                         @PathVariable String id) {
        inboxService.markRead(userPublicId, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/read-all")
    public Map<String, Long> markAllRead(@RequestHeader(USER_HEADER) UUID userPublicId) {
        return Map.of("updated", inboxService.markAllRead(userPublicId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@RequestHeader(USER_HEADER) UUID userPublicId,
                                       @PathVariable String id) {
        inboxService.delete(userPublicId, id);
        return ResponseEntity.noContent().build();
    }
}
