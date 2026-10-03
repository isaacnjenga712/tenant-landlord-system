package com.property.notification.service.channel;

import com.property.notification.domain.UserPreference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InAppChannel implements NotificationChannel {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public String name() { return "IN_APP"; }

    @Override
    public boolean isOptedIn(UserPreference prefs) {
        return prefs.isInAppOptIn();
    }

    @Override
    public void send(UserPreference prefs, String subject, String body) {
        if (prefs.getPublicId() == null) {
            throw new IllegalArgumentException("UserPreference.publicId is null");
        }
        messagingTemplate.convertAndSendToUser(
                prefs.getPublicId().toString(),
                "/queue/notifications",
                body
        );
        log.info("[IN_APP] → user {} :: {}", prefs.getPublicId(), body);
    }
}
