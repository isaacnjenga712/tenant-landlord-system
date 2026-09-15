package com.apex.auth.service;

import com.apex.auth.entity.OutboxEvent;
import com.apex.auth.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxPublisherTest {

    @Mock private OutboxEventRepository outboxRepository;
    @Mock private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks private OutboxPublisher publisher;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(publisher, "userEventsTopic", "auth.user.events");
        ReflectionTestUtils.setField(publisher, "batchSize", 100);
        ReflectionTestUtils.setField(publisher, "enabled", true);
    }

    @Test
    @DisplayName("drainOutbox() marks event published after successful send")
    void drainOutbox_success() {
        OutboxEvent row = OutboxEvent.builder()
                .id(1L).aggregateType("User").aggregateId("42")
                .eventType("user.registered").payload("{\"x\":1}")
                .published(false).createdAt(Instant.now()).build();

        when(outboxRepository.findTop100ByPublishedFalseOrderByCreatedAtAsc())
                .thenReturn(List.of(row));
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        publisher.drainOutbox();

        assertThat(row.isPublished()).isTrue();
        verify(outboxRepository).save(row);
    }

    @Test
    @DisplayName("drainOutbox() leaves row unpublished when send fails")
    void drainOutbox_failureRetriesNextTick() {
        OutboxEvent row = OutboxEvent.builder()
                .id(1L).aggregateType("User").aggregateId("42")
                .eventType("user.registered").payload("{}")
                .published(false).createdAt(Instant.now()).build();

        when(outboxRepository.findTop100ByPublishedFalseOrderByCreatedAtAsc())
                .thenReturn(List.of(row));
        CompletableFuture<SendResult<String, Object>> failed = new CompletableFuture<>();
        failed.completeExceptionally(new RuntimeException("broker down"));
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(failed);

        publisher.drainOutbox();

        assertThat(row.isPublished()).isFalse();
        verify(outboxRepository, never()).save(row);
    }

    @Test
    @DisplayName("drainOutbox() is a no-op when disabled")
    void drainOutbox_disabled() {
        ReflectionTestUtils.setField(publisher, "enabled", false);
        publisher.drainOutbox();
        verifyNoInteractions(outboxRepository, kafkaTemplate);
    }

    @Test
    @DisplayName("drainOutbox() is a no-op when nothing pending")
    void drainOutbox_empty() {
        when(outboxRepository.findTop100ByPublishedFalseOrderByCreatedAtAsc())
                .thenReturn(List.of());
        publisher.drainOutbox();
        verifyNoInteractions(kafkaTemplate);
    }
}
