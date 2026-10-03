package com.apex.module.lease.service.producer;

import com.apex.module.lease.dto.response.LeaseResponse;
import com.platform.common.events.lease.LeaseCreatedEvent;
import com.platform.common.events.lease.LeaseTerminatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaseEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishLeaseCreated(LeaseResponse lease) {
        LeaseCreatedEvent event = new LeaseCreatedEvent();
        event.setLeaseId(lease.getId());
        event.setPropertyId(lease.getPropertyId());
        event.setTenantId(lease.getTenantId());
        event.setLandlordId(lease.getLandlordId());
        event.setStartDate(lease.getStartDate());
        event.setEndDate(lease.getEndDate());
        event.setMonthlyRent(lease.getRentAmount());
        event.setCorrelationId(UUID.randomUUID());
        event.setEventType("lease.lease.created");
        kafkaTemplate.send("lease.lease.created", lease.getId().toString(), event);
        log.info("Published LeaseCreatedEvent for lease {}", lease.getId());
    }

    /** Approval — reuses LeaseCreatedEvent with a different eventType. */
    public void publishLeaseApproved(LeaseResponse lease) {
        LeaseCreatedEvent event = new LeaseCreatedEvent();
        event.setLeaseId(lease.getId());
        event.setPropertyId(lease.getPropertyId());
        event.setTenantId(lease.getTenantId());
        event.setLandlordId(lease.getLandlordId());
        event.setStartDate(lease.getStartDate());
        event.setEndDate(lease.getEndDate());
        event.setMonthlyRent(lease.getRentAmount());
        event.setCorrelationId(UUID.randomUUID());
        event.setEventType("lease.lease.approved");
        kafkaTemplate.send("lease.lease.approved", lease.getId().toString(), event);
        log.info("Published LeaseApprovedEvent for lease {}", lease.getId());
    }

    public void publishLeaseTerminated(LeaseResponse lease, LocalDate terminationDate, String reason) {
        LeaseTerminatedEvent event = new LeaseTerminatedEvent();
        event.setLeaseId(lease.getId());
        event.setPropertyId(lease.getPropertyId());
        event.setTenantId(lease.getTenantId());
        event.setLandlordId(lease.getLandlordId());
        event.setTerminationDate(terminationDate);
        event.setReason(reason);
        event.setCorrelationId(UUID.randomUUID());
        event.setEventType("lease.lease.terminated");
        kafkaTemplate.send("lease.lease.terminated", lease.getId().toString(), event);
        log.info("Published LeaseTerminatedEvent for lease {}", lease.getId());
    }

    // ---- saga stubs (log-only) ----
    public void publishLeaseCreationRequested(LeaseResponse lease) {
        log.info("[SAGA] LeaseCreationRequested for {}", lease.getId());
    }
    public void publishTenantValidationRequested(LeaseResponse lease) {
        log.info("[SAGA] TenantValidationRequested for {}", lease.getId());
    }
    public void publishLeaseCreationFailed(LeaseResponse lease, String message) {
        log.warn("[SAGA] LeaseCreationFailed for {}: {}", lease.getId(), message);
    }
    public void publishCancelPropertyReservation(LeaseResponse lease) {
        log.info("[SAGA] CancelPropertyReservation for {}", lease.getId());
    }
}