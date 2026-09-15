package com.apex.auth.service;

import com.apex.auth.entity.Role;
import com.apex.auth.entity.User;
import com.apex.auth.event.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaEventPublisherTest {

    @Mock private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks private KafkaEventPublisher publisher;

    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(publisher, "userEventsTopic", "auth.user.events");
        user = User.builder().id(1L).email("t@x.com").fullName("Test")
                .role(Role.TENANT).enabled(true).build();
    }

    @SuppressWarnings("unchecked")
    private void stubSend() {
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));
    }

    @Test
    @DisplayName("publishUserRegistered() sends with email key and correct eventType")
    void publishUserRegistered() {
        stubSend();
        publisher.publishUserRegistered(user);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate).send(eq("auth.user.events"), eq("t@x.com"), captor.capture());

        UserRegisteredEvent event = (UserRegisteredEvent) captor.getValue();
        assertThat(event.getEventType()).isEqualTo("user.registered");
        assertThat(event.getEventVersion()).isEqualTo(1);
        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getUserId()).isEqualTo(1L);
        assertThat(event.getEmail()).isEqualTo("t@x.com");
        assertThat(event.getRole()).isEqualTo(Role.TENANT);
    }

    @Test
    @DisplayName("publishUserLoggedIn() sends login event")
    void publishUserLoggedIn() {
        stubSend();
        publisher.publishUserLoggedIn(user);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate).send(eq("auth.user.events"), eq("t@x.com"), captor.capture());

        UserLoggedInEvent event = (UserLoggedInEvent) captor.getValue();
        assertThat(event.getEventType()).isEqualTo("user.login");
        assertThat(event.getLoginAt()).isNotNull();
    }

    @Test
    @DisplayName("publishUserPasswordChanged() sends password.changed event")
    void publishUserPasswordChanged() {
        stubSend();
        publisher.publishUserPasswordChanged(user);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate).send(eq("auth.user.events"), eq("t@x.com"), captor.capture());

        assertThat(captor.getValue()).isInstanceOf(UserPasswordChangedEvent.class);
        assertThat(((UserPasswordChangedEvent) captor.getValue()).getEventType())
                .isEqualTo("user.password.changed");
    }

    @Test
    @DisplayName("publishUserRoleChanged() captures old and new role")
    void publishUserRoleChanged() {
        stubSend();
        publisher.publishUserRoleChanged(user, Role.TENANT, Role.LANDLORD);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate).send(eq("auth.user.events"), eq("t@x.com"), captor.capture());

        UserRoleChangedEvent event = (UserRoleChangedEvent) captor.getValue();
        assertThat(event.getOldRole()).isEqualTo(Role.TENANT);
        assertThat(event.getNewRole()).isEqualTo(Role.LANDLORD);
        assertThat(event.getEventType()).isEqualTo("user.role.changed");
    }

    @Test
    @DisplayName("publishUserDisabled() includes reason")
    void publishUserDisabled() {
        stubSend();
        publisher.publishUserDisabled(user, "policy violation");

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate).send(eq("auth.user.events"), eq("t@x.com"), captor.capture());

        UserDisabledEvent event = (UserDisabledEvent) captor.getValue();
        assertThat(event.getReason()).isEqualTo("policy violation");
        assertThat(event.getEventType()).isEqualTo("user.disabled");
    }

    @Test
    @DisplayName("Each event gets a unique eventId")
    void uniqueEventIds() {
        stubSend();
        publisher.publishUserRegistered(user);
        publisher.publishUserRegistered(user);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate, times(2)).send(anyString(), anyString(), captor.capture());

        UserRegisteredEvent first = (UserRegisteredEvent) captor.getAllValues().get(0);
        UserRegisteredEvent second = (UserRegisteredEvent) captor.getAllValues().get(1);
        assertThat(first.getEventId()).isNotEqualTo(second.getEventId());
    }
}
