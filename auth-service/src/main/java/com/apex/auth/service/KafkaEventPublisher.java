package com.apex.auth.service;

import com.apex.auth.entity.Role;
import com.apex.auth.entity.User;
import com.apex.auth.event.UserDisabledEvent;
import com.apex.auth.event.UserLoggedInEvent;
import com.apex.auth.event.UserPasswordChangedEvent;
import com.apex.auth.event.UserRegisteredEvent;
import com.apex.auth.event.UserRoleChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Publishes domain events to the {@code auth.user.events} Kafka topic.
 *
 * <p>Design notes:
 * <ul>
 *   <li>Each event carries a UUID {@code eventId} for consumer-side deduplication.</li>
 *   <li>{@code eventType} and {@code eventVersion} are stamped here so callers don't have to.</li>
 *   <li>The Kafka key is the user's email → guarantees per-user ordering (all events
 *       for the same user land in the same partition).</li>
 *   <li>Sends are asynchronous; failures are logged but do NOT block the caller.
 *       For strict delivery guarantees, use the outbox pattern via {@code OutboxPublisher}.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topic.user-events}")
    private String userEventsTopic;

    // ============================================================
    // PUBLIC API — one method per event type
    // ============================================================

    /**
     * Emitted after a new user account is persisted.
     * Kafka key: user email.
     */
    public void publishUserRegistered(User user) {
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .eventId(UUID.randomUUID())
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .registeredAt(Instant.now())
                .eventType("user.registered")
                .eventVersion(1)
                .build();

        publish(user.getEmail(), event);
    }

    /**
     * Emitted after a successful authentication.
     * Kafka key: user email.
     */
    public void publishUserLoggedIn(User user) {
        UserLoggedInEvent event = UserLoggedInEvent.builder()
                .eventId(UUID.randomUUID())
                .userId(user.getId())
                .email(user.getEmail())
                .loginAt(Instant.now())
                .eventType("user.login")
                .eventVersion(1)
                .build();

        publish(user.getEmail(), event);
    }

    /**
     * Emitted after a user's password is changed (self-service or reset).
     * Consumers typically revoke all sessions and send a security alert email.
     * Kafka key: user email.
     */
    public void publishUserPasswordChanged(User user) {
        UserPasswordChangedEvent event = UserPasswordChangedEvent.builder()
                .eventId(UUID.randomUUID())
                .userId(user.getId())
                .email(user.getEmail())
                .changedAt(Instant.now())
                .eventType("user.password.changed")
                .eventVersion(1)
                .build();

        publish(user.getEmail(), event);
    }

    /**
     * Emitted after an admin changes a user's role.
     * Captures both old and new role so consumers can react to either side.
     * Kafka key: user email.
     */
    public void publishUserRoleChanged(User user, Role oldRole, Role newRole) {
        UserRoleChangedEvent event = UserRoleChangedEvent.builder()
                .eventId(UUID.randomUUID())
                .userId(user.getId())
                .email(user.getEmail())
                .oldRole(oldRole)
                .newRole(newRole)
                .changedAt(Instant.now())
                .eventType("user.role.changed")
                .eventVersion(1)
                .build();

        publish(user.getEmail(), event);
    }

    /**
     * Emitted after an admin disables a user account.
     * Consumers typically revoke active tokens and stop billing/metering.
     * Kafka key: user email.
     */
    public void publishUserDisabled(User user, String reason) {
        UserDisabledEvent event = UserDisabledEvent.builder()
                .eventId(UUID.randomUUID())
                .userId(user.getId())
                .email(user.getEmail())
                .reason(reason)
                .disabledAt(Instant.now())
                .eventType("user.disabled")
                .eventVersion(1)
                .build();

        publish(user.getEmail(), event);
    }

    // ============================================================
    // CORE PUBLISH LOGIC
    // ============================================================

    /**
     * Sends the event asynchronously and logs the outcome.
     * The HTTP request thread returns immediately — the caller does not block.
     *
     * @param key   Kafka partition key (user email — ensures per-user ordering)
     * @param event the event payload (serialized as JSON by {@code JsonSerializer})
     */
    private void publish(String key, Object event) {
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(userEventsTopic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("📤 Published event={} key={} topic={} partition={} offset={}",
                        event.getClass().getSimpleName(),
                        key,
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("❌ Failed to publish event={} key={} topic={} — {}",
                        event.getClass().getSimpleName(),
                        key,
                        userEventsTopic,
                        ex.getMessage(),
                        ex);
            }
        });
    }
}
