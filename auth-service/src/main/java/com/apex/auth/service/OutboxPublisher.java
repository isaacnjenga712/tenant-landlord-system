package com.apex.auth.service;

import com.apex.auth.entity.OutboxEvent;
import com.apex.auth.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Transactional Outbox publisher.
 *
 * <p>Domain services write to the `outbox_events` table in the SAME transaction
 * as the domain change (insert user, insert outbox row). This class periodically
 * reads unpublished rows and sends them to Kafka, marking each as published only
 * after the broker acknowledges.</p>
 *
 * <p>This guarantees at-least-once delivery: if the send fails, the row remains
 * unpublished and will be retried on the next tick.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxEventRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topic.user-events}")
    private String userEventsTopic;

    @Value("${app.kafka.outbox.batch-size:100}")
    private int batchSize;

    @Value("${app.kafka.outbox.enabled:true}")
    private boolean enabled;

    /**
     * Runs every 5 seconds (configurable via app.kafka.outbox.fixed-delay-ms).
     * Drains up to `batchSize` unpublished rows per tick.
     */
    @Scheduled(
            fixedDelayString = "${app.kafka.outbox.fixed-delay-ms:5000}",
            initialDelayString = "${app.kafka.outbox.initial-delay-ms:10000}"
    )
    public void drainOutbox() {
        if (!enabled) {
            return;
        }

        List<OutboxEvent> pending = outboxRepository
                .findTop100ByPublishedFalseOrderByCreatedAtAsc();

        if (pending.isEmpty()) {
            return;
        }

        log.debug("Outbox drain: {} pending events", pending.size());

        for (OutboxEvent event : pending) {
            publishOne(event);
        }
    }

    // ---------------------------------------------------------------
    // Per-event publish — isolated transaction so one bad row
    // doesn't roll back the others.
    // ---------------------------------------------------------------

    @Transactional
    public void publishOne(OutboxEvent event) {
        try {
            // Use aggregateId as the Kafka key → ordering per aggregate
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(userEventsTopic, event.getAggregateId(), event.getPayload());

            // Block until the broker acknowledges (bounded by producer timeouts)
            future.get();

            event.setPublished(true);
            outboxRepository.save(event);

            log.info("Outbox published: id={} type={} aggregateId={}",
                    event.getId(), event.getEventType(), event.getAggregateId());

        } catch (Exception ex) {
            log.error("Outbox publish FAILED for id={} type={} — will retry next tick",
                    event.getId(), event.getEventType(), ex);
            // Do not mark as published; the next scheduled tick will retry
        }
    }

    // ---------------------------------------------------------------
    // Cleanup — purge published rows older than 7 days
    // ---------------------------------------------------------------

    @Scheduled(cron = "${app.kafka.outbox.cleanup-cron:0 0 3 * * *}")  // 3 AM daily
    @Transactional
    public void purgePublished() {
        if (!enabled) {
            return;
        }
        int deleted = outboxRepository.deleteByPublishedTrueAndCreatedAtBefore(
                java.time.Instant.now().minusSeconds(7L * 24 * 60 * 60)
        );
        if (deleted > 0) {
            log.info("Outbox cleanup: purged {} published events", deleted);
        }
    }
}
