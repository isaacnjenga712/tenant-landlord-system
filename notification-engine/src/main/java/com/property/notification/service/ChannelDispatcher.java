package com.property.notification.service;

import com.property.notification.domain.NotificationLog;
import com.property.notification.domain.UserPreference;
import com.property.notification.dto.NotificationDto;
import com.property.notification.repository.NotificationLogRepository;
import com.property.notification.repository.UserPreferenceRepository;
import com.property.notification.service.channel.NotificationChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChannelDispatcher {

    private static final String IN_APP = "IN_APP";

    private final List<NotificationChannel> channels;
    private final UserPreferenceRepository prefsRepo;
    private final NotificationLogRepository logRepo;
    private final NotificationInboxService inboxService;
    private final SimpMessagingTemplate messagingTemplate;

    public void dispatch(UUID recipientPublicId,
                         String eventType,
                         String subject,
                         String body,
                         String rawPayload) {

        if (recipientPublicId == null) {
            log.debug("Skipping dispatch — recipient UUID is null for event {}", eventType);
            return;
        }

        UserPreference prefs = prefsRepo.findByPublicId(recipientPublicId).orElse(null);
        if (prefs == null) {
            log.info("No preferences found for user {} — skipping dispatch of {}",
                    recipientPublicId, eventType);
            return;
        }

        // ─────────────────────────────────────────────────────────
        // IN_APP — Option A: save to inbox, then push structured DTO
        // ─────────────────────────────────────────────────────────
        if (prefs.isInAppOptIn()) {
            handleInApp(recipientPublicId, eventType, subject, body, rawPayload);
        }

        // ─────────────────────────────────────────────────────────
        // Other channels (EMAIL, SMS, WHATSAPP) — unchanged fan-out
        // ─────────────────────────────────────────────────────────
        for (NotificationChannel channel : channels) {
            if (IN_APP.equals(channel.name())) continue;   // handled above
            if (!channel.isOptedIn(prefs)) continue;

            NotificationLog entry = NotificationLog.builder()
                    .eventType(eventType)
                    .channel(NotificationLog.Channel.valueOf(channel.name()))
                    .status(NotificationLog.Status.PENDING)
                    .payload(rawPayload)
                    .attempts(0)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            try {
                channel.send(prefs, subject, body);
                entry.setStatus(NotificationLog.Status.SENT);
                log.debug("Dispatched {} via {} to {}", eventType, channel.name(), recipientPublicId);
            } catch (Exception e) {
                entry.setStatus(NotificationLog.Status.FAILED);
                entry.setErrorMessage(e.getMessage());
                entry.setAttempts(1);
                log.warn("Failed to send {} via {} to {}: {}",
                        eventType, channel.name(), recipientPublicId, e.getMessage());
            }
            logRepo.save(entry);
        }
    }

    private void handleInApp(UUID recipientPublicId, String eventType,
                             String subject, String body, String rawPayload) {
        NotificationDto saved = null;
        String error = null;

        try {
            // 1. Persist to Mongo → get id
            saved = inboxService.save(recipientPublicId, eventType, subject, body, rawPayload);

            // 2. Push structured DTO to the user's STOMP queue
            messagingTemplate.convertAndSendToUser(
                    recipientPublicId.toString(),
                    "/queue/notifications",
                    saved);

            log.debug("IN_APP pushed id={} to {}", saved.getId(), recipientPublicId);
        } catch (Exception e) {
            error = e.getMessage();
            log.warn("IN_APP failed for {}: {}", recipientPublicId, e.getMessage());
        }

        // 3. Audit log
        logRepo.save(NotificationLog.builder()
                .eventType(eventType)
                .channel(NotificationLog.Channel.IN_APP)
                .status(saved != null ? NotificationLog.Status.SENT : NotificationLog.Status.FAILED)
                .payload(rawPayload)
                .attempts(saved != null ? 0 : 1)
                .errorMessage(error)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
    }
}