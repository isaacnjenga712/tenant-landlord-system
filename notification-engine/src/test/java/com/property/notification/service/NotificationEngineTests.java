package com.property.notification.service;

import com.property.notification.NotificationEngineApplication;
import com.property.notification.domain.NotificationLog;
import com.property.notification.domain.UserPreference;
import com.property.notification.dto.MaintenanceEventDto;
import com.property.notification.dto.PaymentEventDto;
import com.property.notification.repository.NotificationLogRepository;
import com.property.notification.repository.UserPreferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = NotificationEngineApplication.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EmbeddedKafka(partitions = 1, topics = {"payment-events", "maintenance-events"})
@TestPropertySource(properties = {
        "spring.kafka.consumer.group-id=test-group-${random.uuid}",
        "spring.kafka.consumer.auto-offset-reset=earliest"
})
public class NotificationEngineTests {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private NotificationLogRepository logRepository;

    @MockBean
    private UserPreferenceRepository preferenceRepository;

    @MockBean
    private EmailService emailService;

    @MockBean
    private SmsService smsService;

    @MockBean
    private InAppNotificationService inAppService;

    private final Long userId = 1L;
    private final String propertyId = UUID.randomUUID().toString();

    @BeforeEach
    void setup() {
        logRepository.deleteAll();

        UserPreference prefs = UserPreference.builder()
                .userId(userId)
                .emailOptIn(true)
                .smsOptIn(true)
                .inAppOptIn(true)
                .email("tenant@example.com")
                .phone("+1234567890")
                .build();
        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.of(prefs));
    }

    @Test
    void testRentPaymentEventTriggersAllChannels() {
        PaymentEventDto dto = PaymentEventDto.builder()
                .userId(userId)
                .propertyId(propertyId)
                .amount(BigDecimal.valueOf(1200.00))
                .paymentMethod("credit_card")
                .build();

        kafkaTemplate.send("payment-events", userId.toString(), dto);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(emailService, times(1)).sendTemplateEmail(
                    eq("tenant@example.com"),
                    eq("Rent Payment Received"),
                    eq("payment-receipt"),
                    anyMap()
            );
            verify(smsService, times(1)).sendSms(
                    eq("+1234567890"),
                    contains("1200")   // amount appears without decimal
            );
            verify(inAppService, times(1)).sendToUser(
                    eq(userId),
                    contains("$1200")
            );
            List<NotificationLog> logs = logRepository.findAll();
            assertThat(logs).hasSize(3);
            assertThat(logs).allMatch(log -> log.getStatus() == NotificationLog.Status.SENT);
        });
    }

    @Test
    void testMaintenanceRequestEventTriggersAllChannels() {
        MaintenanceEventDto dto = MaintenanceEventDto.builder()
                .userId(userId)
                .propertyId(propertyId)
                .description("Leaking pipe in bathroom")
                .build();

        kafkaTemplate.send("maintenance-events", userId.toString(), dto);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(emailService, times(1)).sendTemplateEmail(
                    eq("tenant@example.com"),
                    eq("Maintenance Request Created"),
                    eq("invoice-alert"),
                    anyMap()
            );
            verify(smsService, times(1)).sendSms(
                    eq("+1234567890"),
                    contains("Leaking pipe")
            );
            verify(inAppService, times(1)).sendToUser(
                    eq(userId),
                    contains("Leaking pipe")
            );
            List<NotificationLog> logs = logRepository.findAll();
            assertThat(logs).hasSize(3);
            assertThat(logs).allMatch(log -> log.getStatus() == NotificationLog.Status.SENT);
        });
    }

    @Test
    void testEmailServiceFailureLogsAsFailed() {
        doThrow(new RuntimeException("SMTP error")).when(emailService).sendTemplateEmail(
                anyString(), anyString(), anyString(), anyMap()
        );

        PaymentEventDto dto = PaymentEventDto.builder()
                .userId(userId)
                .propertyId(propertyId)
                .amount(BigDecimal.valueOf(500))
                .paymentMethod("paypal")
                .build();

        kafkaTemplate.send("payment-events", userId.toString(), dto);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            List<NotificationLog> logs = logRepository.findAll();
            assertThat(logs).hasSize(3);

            NotificationLog emailLog = logs.stream()
                    .filter(l -> l.getChannel() == NotificationLog.Channel.EMAIL)
                    .findFirst().orElseThrow();
            assertThat(emailLog.getStatus()).isEqualTo(NotificationLog.Status.FAILED);
            assertThat(emailLog.getErrorMessage()).contains("SMTP error");

            assertThat(logs.stream().filter(l -> l.getChannel() == NotificationLog.Channel.SMS))
                    .allMatch(l -> l.getStatus() == NotificationLog.Status.SENT);
            assertThat(logs.stream().filter(l -> l.getChannel() == NotificationLog.Channel.IN_APP))
                    .allMatch(l -> l.getStatus() == NotificationLog.Status.SENT);
        });
    }

    @Test
    void testUserWithoutPreferencesThrowsException() {
        Long unknownUserId = 999L;
        when(preferenceRepository.findByUserId(unknownUserId)).thenReturn(Optional.empty());

        PaymentEventDto dto = PaymentEventDto.builder()
                .userId(unknownUserId)
                .propertyId(propertyId)
                .amount(BigDecimal.TEN)
                .paymentMethod("cash")
                .build();

        kafkaTemplate.send("payment-events", unknownUserId.toString(), dto);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(emailService, never()).sendTemplateEmail(anyString(), anyString(), anyString(), anyMap());
            verify(smsService, never()).sendSms(anyString(), anyString());
            verify(inAppService, never()).sendToUser(anyLong(), anyString());
            assertThat(logRepository.findAll()).isEmpty();
        });
    }
}