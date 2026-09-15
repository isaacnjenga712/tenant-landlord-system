package com.property.notification.listener;

import com.property.notification.domain.NotificationLog;
import com.property.notification.domain.UserPreference;
import com.property.notification.dto.MaintenanceEventDto;
import com.property.notification.dto.PaymentEventDto;
import com.property.notification.repository.NotificationLogRepository;
import com.property.notification.repository.UserPreferenceRepository;
import com.property.notification.service.EmailService;
import com.property.notification.service.InAppNotificationService;
import com.property.notification.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaNotificationConsumer {

    private final UserPreferenceRepository preferenceRepository;
    private final NotificationLogRepository logRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    private final InAppNotificationService inAppService;

    @KafkaListener(topics = "${app.kafka.topics.payment}", groupId = "${spring.kafka.consumer.group-id}")
    public void handlePaymentEvent(PaymentEventDto event) {
        Long userId = event.getUserId();
        UserPreference prefs = preferenceRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User preferences not found for userId: " + userId));

        Map<String, Object> model = new HashMap<>();
        model.put("amount", event.getAmount());
        model.put("propertyId", event.getPropertyId());
        model.put("method", event.getPaymentMethod());

        if (prefs.isEmailOptIn()) {
            sendEmail(userId, "payment-receipt", "Rent Payment Received", model, prefs.getEmail());
        }
        if (prefs.isSmsOptIn()) {
            sendSms(userId, "Rent payment of " + event.getAmount() + " received for property " + event.getPropertyId(), prefs.getPhone());
        }
        if (prefs.isInAppOptIn()) {
            sendInApp(userId, "Rent payment received: $" + event.getAmount());
        }
    }

    @KafkaListener(topics = "${app.kafka.topics.maintenance}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleMaintenanceEvent(MaintenanceEventDto event) {
        Long userId = event.getUserId();
        UserPreference prefs = preferenceRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User preferences not found for userId: " + userId));

        Map<String, Object> model = new HashMap<>();
        model.put("propertyId", event.getPropertyId());
        model.put("description", event.getDescription());

        if (prefs.isEmailOptIn()) {
            sendEmail(userId, "invoice-alert", "Maintenance Request Created", model, prefs.getEmail());
        }
        if (prefs.isSmsOptIn()) {
            sendSms(userId, "Maintenance request for property " + event.getPropertyId() + ": " + event.getDescription(), prefs.getPhone());
        }
        if (prefs.isInAppOptIn()) {
            sendInApp(userId, "Maintenance request: " + event.getDescription());
        }
    }

    // ------------------- Helper methods (copied from NotificationEventListener) -------------------

    private void sendEmail(Long userId, String template, String subject, Map<String, Object> model, String to) {
        NotificationLog log = createLog(userId, NotificationLog.Channel.EMAIL, "EMAIL");
        try {
            emailService.sendTemplateEmail(to, subject, template, model);
            log.setStatus(NotificationLog.Status.SENT);
        } catch (Exception e) {
            log.setStatus(NotificationLog.Status.FAILED);
            log.setErrorMessage(e.getMessage());
            log.setAttempts(log.getAttempts() + 1);
        }
        logRepository.save(log);
    }

    private void sendSms(Long userId, String message, String phone) {
        NotificationLog log = createLog(userId, NotificationLog.Channel.SMS, "SMS");
        try {
            smsService.sendSms(phone, message);
            log.setStatus(NotificationLog.Status.SENT);
        } catch (Exception e) {
            log.setStatus(NotificationLog.Status.FAILED);
            log.setErrorMessage(e.getMessage());
            log.setAttempts(log.getAttempts() + 1);
        }
        logRepository.save(log);
    }

    private void sendInApp(Long userId, String message) {
        NotificationLog log = createLog(userId, NotificationLog.Channel.IN_APP, "IN_APP");
        try {
            inAppService.sendToUser(userId, message);
            log.setStatus(NotificationLog.Status.SENT);
        } catch (Exception e) {
            log.setStatus(NotificationLog.Status.FAILED);
            log.setErrorMessage(e.getMessage());
            log.setAttempts(log.getAttempts() + 1);
        }
        logRepository.save(log);
    }

    private NotificationLog createLog(Long userId, NotificationLog.Channel channel, String eventType) {
        return NotificationLog.builder()
                .userId(userId)
                .channel(channel)
                .status(NotificationLog.Status.PENDING)
                .eventType(eventType)
                .attempts(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}