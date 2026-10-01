package com.property.notification.service;

import com.property.notification.domain.NotificationLog;
import com.property.notification.domain.UserPreference;
import com.property.notification.repository.NotificationLogRepository;
import com.property.notification.repository.UserPreferenceRepository;
import com.property.notification.service.channel.NotificationChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChannelDispatcher {

    private final List<NotificationChannel> channels;
    private final UserPreferenceRepository prefsRepo;
    private final NotificationLogRepository logRepo;

    /**
     * Fan out one event to all applicable channels for a single recipient.
     *
     * @param recipientPublicId  user to notify
     * @param eventType          canonical event type
     * @param subject            short subject (email header)
     * @param body               message body
     * @param rawPayload         original payload for audit
     */
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

        for (NotificationChannel channel : channels) {
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
}
