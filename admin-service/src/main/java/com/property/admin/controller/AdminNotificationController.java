package com.property.admin.controller;

import com.platform.common.dtos.ApiResponse;
import com.platform.common.dtos.PageResponse;
import com.platform.common.dtos.admin.NotificationLogDto;
import com.property.admin.service.AdminNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final AdminNotificationService service;

    @GetMapping("/logs")
    public ApiResponse<PageResponse<NotificationLogDto>> logs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) String status) {
        return ApiResponse.ok(service.getLogs(page, size, eventType, channel, status));
    }

    @PostMapping("/retry/{id}")
    public ApiResponse<Void> retry(@PathVariable String id) {
        service.retry(id);
        return ApiResponse.ok(null);
    }
}