package com.property.notification.scheduler;

import com.property.notification.domain.NotificationLog;
import com.property.notification.domain.UserPreference;
import com.property.notification.repository.NotificationLogRepository;
import com.property.notification.repository.UserPreferenceRepository;
import com.property.notification.service.EmailService;
import com.property.notification.service.InAppNotificationService;
import com.property.notification.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationRecoveryScheduler {

    private final NotificationLogRepository logRepository;
    private final UserPreferenceRepository preferenceRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    private final InAppNotificationService inAppService;

    private static final int MAX_ATTEMPTS = 3;

    @Scheduled(cron = "0 */5 * * * ?")
    public void recoverFailedNotifications() {
        List<NotificationLog> pendingOrFailed = logRepository.findByStatusAndAttemptsLessThan(
                NotificationLog.Status.FAILED, MAX_ATTEMPTS
        );
        pendingOrFailed.addAll(logRepository.findByStatusAndAttemptsLessThan(
                NotificationLog.Status.PENDING, MAX_ATTEMPTS
        ));

        if (pendingOrFailed.isEmpty()) {
            log.debug("No pending or failed notifications to recover.");
            return;
        }

        for (NotificationLog notificationLog : pendingOrFailed) {
            retryNotification(notificationLog);
        }
    }

    private void retryNotification(NotificationLog notificationLog) {
        notificationLog.setAttempts(notificationLog.getAttempts() + 1);
        try {
            UserPreference prefs = preferenceRepository.findByUserId(notificationLog.getUserId())
                    .orElseThrow(() -> new IllegalStateException("No preferences for user " + notificationLog.getUserId()));

            switch (notificationLog.getChannel()) {
                case EMAIL -> {
                    emailService.sendTemplateEmail(
                            prefs.getEmail(),
                            "Retry: " + notificationLog.getEventType(),
                            "invoice-alert", // fallback template
                            Map.of()
                    );
                }
                case SMS -> {
                    smsService.sendSms(prefs.getPhone(), "Retry notification: " + notificationLog.getEventType());
                }
                case IN_APP -> {
                    inAppService.sendToUser(notificationLog.getUserId(), "Retry notification: " + notificationLog.getEventType());
                }
            }
            notificationLog.setStatus(NotificationLog.Status.SENT);
            notificationLog.setErrorMessage(null);
            log.info("Successfully retried notification log id: {}", notificationLog.getId());
        } catch (Exception e) {
            notificationLog.setStatus(NotificationLog.Status.FAILED);
            notificationLog.setErrorMessage(e.getMessage());
            log.error("Failed to retry notification log id: {} – {}", notificationLog.getId(), e.getMessage());
        }
        notificationLog.setUpdatedAt(LocalDateTime.now());
        logRepository.save(notificationLog);
    }
}
