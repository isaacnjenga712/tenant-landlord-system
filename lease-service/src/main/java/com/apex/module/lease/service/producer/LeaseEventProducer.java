package com.apex.module.lease.service.producer;

import com.apex.module.lease.dto.response.LeaseResponse;
import com.apex.module.lease.enums.LeaseEventType;
import com.apex.module.lease.event.LeaseEvent;
import com.apex.module.lease.event.LeaseEventPayload;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
public class LeaseEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String PROPERTY_EVENTS_TOPIC = "property-events";
    private static final String TENANT_EVENTS_TOPIC = "tenant-events";
    private static final String LEASE_EVENTS_TOPIC = "lease-events";

    // Explicit constructor (replaces @RequiredArgsConstructor)
    public LeaseEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishLeaseCreationRequested(LeaseResponse lease) {
        LeaseEvent event = buildEvent(LeaseEventType.LEASE_CREATION_REQUESTED, lease);
        String key = Objects.requireNonNull(lease.getId(), "Lease ID must not be null").toString();
        kafkaTemplate.send(PROPERTY_EVENTS_TOPIC, key, event);
    }

    public void publishTenantValidationRequested(LeaseResponse lease) {
        LeaseEvent event = buildEvent(LeaseEventType.TENANT_VALIDATION_REQUESTED, lease);
        String key = Objects.requireNonNull(lease.getId(), "Lease ID must not be null").toString();
        kafkaTemplate.send(TENANT_EVENTS_TOPIC, key, event);
    }

    public void publishLeaseCreated(LeaseResponse lease) {
        LeaseEvent event = buildEvent(LeaseEventType.LEASE_CREATED, lease);
        String key = Objects.requireNonNull(lease.getId(), "Lease ID must not be null").toString();
        kafkaTemplate.send(LEASE_EVENTS_TOPIC, key, event);
    }

    public void publishLeaseCreationFailed(LeaseResponse lease, String reason) {
        LeaseEvent event = buildEvent(LeaseEventType.LEASE_CREATION_FAILED, lease);
        event.getPayload().setFailureReason(reason);
        String key = Objects.requireNonNull(lease.getId(), "Lease ID must not be null").toString();
        kafkaTemplate.send(LEASE_EVENTS_TOPIC, key, event);
    }

    public void publishCancelPropertyReservation(LeaseResponse lease) {
        LeaseEvent event = buildEvent(LeaseEventType.CANCEL_PROPERTY_RESERVATION, lease);
        String key = Objects.requireNonNull(lease.getId(), "Lease ID must not be null").toString();
        kafkaTemplate.send(PROPERTY_EVENTS_TOPIC, key, event);
    }

    private LeaseEvent buildEvent(LeaseEventType type, LeaseResponse lease) {
        LeaseEvent event = new LeaseEvent();
        event.setEventId(UUID.randomUUID());
        event.setEventType(type);
        event.setTimestamp(LocalDateTime.now());
        event.setSource("lease-service");
        event.setPayload(new LeaseEventPayload(lease));
        return event;
    }
}