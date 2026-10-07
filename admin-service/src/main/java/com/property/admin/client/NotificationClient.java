package com.property.admin.client;

import com.platform.common.dtos.PageResponse;
import com.platform.common.dtos.admin.NotificationLogDto;
import com.property.admin.client.fallback.NotificationClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "NOTIFICATION-ENGINE",
        contextId = "adminNotificationClient",
        path = "/api/v1/internal",
        fallback = NotificationClientFallback.class
)
public interface NotificationClient {

    @GetMapping("/notifications/logs")
    PageResponse<NotificationLogDto> logs(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam(value = "eventType", required = false) String eventType,
            @RequestParam(value = "channel", required = false) String channel,
            @RequestParam(value = "status", required = false) String status);

    @PostMapping("/notifications/retry/{id}")
    void retry(@PathVariable("id") String id);
}