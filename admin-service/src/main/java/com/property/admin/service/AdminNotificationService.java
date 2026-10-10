package com.property.admin.service;

import com.platform.common.dtos.PageResponse;
import com.platform.common.dtos.admin.NotificationLogDto;
import com.property.admin.client.NotificationClient;
import com.property.admin.exception.UpstreamServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminNotificationService {

    private final NotificationClient notificationClient;

    public PageResponse<NotificationLogDto> getLogs(int page, int size,
                                                     String eventType, String channel,
                                                     String status) {
        return notificationClient.logs(page, size, eventType, channel, status);
    }

    public void retry(String id) {
        try {
            notificationClient.retry(id);
        } catch (Exception e) {
            throw new UpstreamServiceException("notification-engine", e);
        }
    }
}