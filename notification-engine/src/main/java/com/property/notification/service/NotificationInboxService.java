package com.property.notification.service;

import com.property.notification.domain.Notification;
import com.property.notification.dto.NotificationDto;
import com.property.notification.dto.PageResponse;
import com.property.notification.exception.ResourceNotFoundException;
import com.property.notification.repository.NotificationRepository;
import com.platform.common.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationInboxService {

    private final NotificationRepository repo;

    /** Called from ChannelDispatcher (Kafka path). orgId comes from the event. */
    public NotificationDto save(UUID organizationId, UUID recipientPublicId, String eventType,
                                String subject, String body, String rawPayload) {
        Notification n = Notification.builder()
                .organizationId(organizationId)
                .recipientPublicId(recipientPublicId)
                .eventType(eventType)
                .subject(subject)
                .body(body)
                .rawPayload(rawPayload)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
        return toDto(repo.save(n));
    }

    public PageResponse<NotificationDto> list(UUID recipientPublicId, int page, int size) {
        UUID orgId = TenantContext.require();
        Pageable pageable = PageRequest.of(page, size);
        return PageResponse.of(
                repo.findByOrganizationIdAndRecipientPublicIdOrderByCreatedAtDesc(
                        orgId, recipientPublicId, pageable),
                this::toDto);
    }

    public long unreadCount(UUID recipientPublicId) {
        UUID orgId = TenantContext.require();
        return repo.countByOrganizationIdAndRecipientPublicIdAndReadIsFalse(orgId, recipientPublicId);
    }

    public void markRead(UUID recipientPublicId, String id) {
        UUID orgId = TenantContext.require();
        Notification n = repo.findByIdAndOrganizationId(id, orgId)
                .filter(x -> x.getRecipientPublicId().equals(recipientPublicId))
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!n.isRead()) {
            n.setRead(true);
            n.setReadAt(LocalDateTime.now());
            repo.save(n);
        }
    }

    public long markAllRead(UUID recipientPublicId) {
        UUID orgId = TenantContext.require();
        return repo.markAllReadInOrg(orgId, recipientPublicId, LocalDateTime.now());
    }

    public void delete(UUID recipientPublicId, String id) {
        UUID orgId = TenantContext.require();
        repo.findByIdAndOrganizationId(id, orgId)
                .filter(x -> x.getRecipientPublicId().equals(recipientPublicId))
                .ifPresent(repo::delete);
    }

    public NotificationDto toDto(Notification n) {
        return NotificationDto.builder()
                .id(n.getId())
                .recipientPublicId(n.getRecipientPublicId())
                .eventType(n.getEventType())
                .subject(n.getSubject())
                .body(n.getBody())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .readAt(n.getReadAt())
                .build();
    }
}