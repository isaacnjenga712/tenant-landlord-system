package com.apex.auth.service;

import com.apex.auth.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserEventConsumerTest {

    @Mock private UserRepository userRepository;
    @Mock private Acknowledgment acknowledgment;

    private UserEventConsumer consumer;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        consumer = new UserEventConsumer(userRepository, objectMapper);
    }

    @Test
    @DisplayName("user.registered event is processed and acked")
    void onUserRegistered() {
        String payload = """
                {
                  "eventId":"11111111-1111-1111-1111-111111111111",
                  "userId":1,"email":"t@x.com","fullName":"Test",
                  "role":"TENANT","registeredAt":"2026-09-10T08:00:00Z",
                  "eventType":"user.registered","eventVersion":1
                }
                """;

        consumer.onUserEvent(payload, 0, 0L, acknowledgment);

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("user.login event is processed and acked")
    void onUserLoggedIn() {
        String payload = """
                {
                  "eventId":"22222222-2222-2222-2222-222222222222",
                  "userId":1,"email":"t@x.com","loginAt":"2026-09-10T09:00:00Z",
                  "eventType":"user.login","eventVersion":1
                }
                """;

        consumer.onUserEvent(payload, 0, 1L, acknowledgment);

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("user.disabled event disables the user and acks")
    void onUserDisabled() {
        com.apex.auth.entity.User user = com.apex.auth.entity.User.builder()
                .id(1L).email("t@x.com").password("h").fullName("Test")
                .role(com.apex.auth.entity.Role.TENANT).enabled(true).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        String payload = """
                {
                  "eventId":"33333333-3333-3333-3333-333333333333",
                  "userId":1,"email":"t@x.com","reason":"policy",
                  "disabledAt":"2026-09-10T10:00:00Z",
                  "eventType":"user.disabled","eventVersion":1
                }
                """;

        consumer.onUserEvent(payload, 0, 2L, acknowledgment);

        verify(userRepository).save(user);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Unknown event type is acked without error")
    void unknownEventType() {
        String payload = """
                {
                  "eventId":"44444444-4444-4444-4444-444444444444",
                  "eventType":"some.unknown.type","eventVersion":1
                }
                """;

        consumer.onUserEvent(payload, 0, 3L, acknowledgment);

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Malformed JSON causes retry (no ack)")
    void malformedPayload_throws() {
        try {
            consumer.onUserEvent("not-json", 0, 4L, acknowledgment);
        } catch (RuntimeException expected) {
            // Expected — the error handler will retry
        }
        verify(acknowledgment, never()).acknowledge();
    }
}
