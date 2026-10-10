package com.apex.auth.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public void onUserEvent(String rawPayload, Acknowledgment ack) {
        try {
            JsonNode node = objectMapper.readTree(rawPayload);
            String eventType = node.path("eventType").asText();

            log.info("Received event type={} payload={}", eventType, rawPayload);

            switch (eventType) {
                case "user.registered"       -> handleUserRegistered(rawPayload);
                case "user.login"            -> handleUserLoggedIn(rawPayload);
                case "user.password.changed" -> handlePasswordChanged(rawPayload);
                case "user.role.changed"     -> handleRoleChanged(rawPayload);
                case "user.disabled"         -> handleUserDisabled(rawPayload);
                default -> log.warn("Unhandled event type: {}", eventType);
            }

            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Failed to process user event: {}", ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    private void handleUserRegistered(String payload) throws Exception {
        UserRegisteredEvent event = objectMapper.readValue(payload, UserRegisteredEvent.class);
        log.info("New user registered: id={} email={} role={}",
                event.getUserId(), event.getEmail(), event.getRole());
    }

    private void handleUserLoggedIn(String payload) throws Exception {
        UserLoggedInEvent event = objectMapper.readValue(payload, UserLoggedInEvent.class);
        log.info("User logged in: id={} email={} at={}",
                event.getUserId(), event.getEmail(), event.getLoginAt());
    }

    private void handlePasswordChanged(String payload) throws Exception {
        UserPasswordChangedEvent event = objectMapper.readValue(payload, UserPasswordChangedEvent.class);
        log.info("Password changed for user id={} email={}", event.getUserId(), event.getEmail());
    }

    private void handleRoleChanged(String payload) throws Exception {
        UserRoleChangedEvent event = objectMapper.readValue(payload, UserRoleChangedEvent.class);
        log.info("Role changed for user id={}: {} → {}",
                event.getUserId(), event.getOldRole(), event.getNewRole());
    }

    private void handleUserDisabled(String payload) throws Exception {
        UserDisabledEvent event = objectMapper.readValue(payload, UserDisabledEvent.class);
        log.info("User disabled: id={} email={} reason={}",
                event.getUserId(), event.getEmail(), event.getReason());
        userRepository.findById(event.getUserId()).ifPresent(user -> {
            user.setEnabled(false);
            userRepository.save(user);
        });
    }
}