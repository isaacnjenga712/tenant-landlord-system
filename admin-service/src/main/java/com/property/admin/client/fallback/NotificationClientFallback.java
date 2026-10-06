package com.property.admin.client.fallback;

import com.platform.common.dtos.PageResponse;
import com.platform.common.dtos.admin.NotificationLogDto;
import com.property.admin.client.NotificationClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationClientFallback implements NotificationClient {

    @Override
    public PageResponse<NotificationLogDto> logs(int page, int size,
                                                  String eventType, String channel,
                                                  String status) {
        log.warn("notification-engine unavailable — returning empty logs");
        return PageResponse.empty(page, size);
    }

    @Override
    public void retry(String id) {
        log.warn("notification-engine unavailable — retry skipped for {}", id);
        throw new RuntimeException("notification-engine unavailable");
    }
}