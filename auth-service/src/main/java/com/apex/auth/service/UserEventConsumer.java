package com.apex.auth.service;

import com.apex.auth.entity.User;
import com.apex.auth.event.UserDisabledEvent;
import com.apex.auth.event.UserLoggedInEvent;
import com.apex.auth.event.UserPasswordChangedEvent;
import com.apex.auth.event.UserRegisteredEvent;
import com.apex.auth.event.UserRoleChangedEvent;
import com.apex.auth.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consumes events from the auth.user.events topic.
 *
 * Because multiple event types share one topic, we deserialize into a generic
 * JsonNode first, then route on the "eventType" field. This keeps the topic
 * schema flexible and avoids needing per-type deserialization config.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventConsumer {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${app.kafka.topic.user-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void onUserEvent(
            @Payload String rawPayload,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack
    ) {
        try {
            JsonNode node = objectMapper.readTree(rawPayload);
            String eventType = node.path("eventType").asText();

            log.info("Received event type={} partition={} offset={}",
                    eventType, partition, offset);

            switch (eventType) {
                case "user.registered"       -> handleUserRegistered(rawPayload);
                case "user.login"            -> handleUserLoggedIn(rawPayload);
                case "user.password.changed" -> handlePasswordChanged(rawPayload);
                case "user.role.changed"     -> handleRoleChanged(rawPayload);
                case "user.disabled"         -> handleUserDisabled(rawPayload);
                default -> log.warn("Unhandled event type: {}", eventType);
            }

            // Commit offset only after successful processing
            ack.acknowledge();

        } catch (Exception ex) {
            log.error("Failed to process user event at partition={} offset={}: {}",
                    partition, offset, ex.getMessage(), ex);
            // Do NOT ack — the error handler will retry per the backoff policy
            throw new RuntimeException(ex);
        }
    }

    // ---------------------------------------------------------------
    // Handlers
    // ---------------------------------------------------------------

    private void handleUserRegistered(String payload) throws Exception {
        UserRegisteredEvent event = objectMapper.readValue(payload, UserRegisteredEvent.class);

        log.info("New user registered: id={} email={} role={}",
                event.getUserId(), event.getEmail(), event.getRole());

        // Example side effect: assign default preferences, seed tenant dashboard,
        // or enqueue a welcome email via a downstream notification service.
        // For now, we just log — extend as needed.
    }

    private void handleUserLoggedIn(String payload) throws Exception {
        UserLoggedInEvent event = objectMapper.readValue(payload, UserLoggedInEvent.class);

        log.info("User logged in: id={} email={} at={}",
                event.getUserId(), event.getEmail(), event.getLoginAt());

        // Example: update last_login_at on the user record
        userRepository.findById(event.getUserId()).ifPresent(user -> {
            // user.setLastLoginAt(event.getLoginAt());  // add field if you want
            userRepository.save(user);
        });
    }

    private void handlePasswordChanged(String payload) throws Exception {
        UserPasswordChangedEvent event = objectMapper.readValue(payload, UserPasswordChangedEvent.class);
        log.info("Password changed for user id={} email={}", event.getUserId(), event.getEmail());

        // Example: invalidate all refresh tokens for this user
        // refreshTokenService.deleteByUser(userRepository.getReferenceById(event.getUserId()));
    }

    private void handleRoleChanged(String payload) throws Exception {
        UserRoleChangedEvent event = objectMapper.readValue(payload, UserRoleChangedEvent.class);
        log.info("Role changed for user id={}: {} → {}",
                event.getUserId(), event.getOldRole(), event.getNewRole());

        // Example: log audit trail entry, notify admin service
    }

    private void handleUserDisabled(String payload) throws Exception {
        UserDisabledEvent event = objectMapper.readValue(payload, UserDisabledEvent.class);
        log.info("User disabled: id={} email={} reason={}",
                event.getUserId(), event.getEmail(), event.getReason());

        // Example: revoke all refresh tokens
        userRepository.findById(event.getUserId()).ifPresent(user -> {
            user.setEnabled(false);
            userRepository.save(user);
        });
    }
}
