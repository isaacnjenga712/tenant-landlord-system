package com.apex.leaseService.service;

import com.apex.leaseService.entity.Lease;
import com.apex.leaseService.entity.LeaseStatus;
import com.apex.leaseService.producer.LeaseEventPublisher;
import com.apex.leaseService.repository.LeaseRepository;
import com.platform.common.events.lease.LeaseCreatedEvent;
import com.platform.common.events.lease.LeaseTerminatedEvent;
import com.platform.common.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaseCommandService {

    private final LeaseRepository leaseRepository;
    private final LeaseEventPublisher eventPublisher;

    @Transactional
    public Lease createLease(String propertyId, String tenantId, String landlordId,
                             LocalDate startDate, LocalDate endDate,
                             BigDecimal monthlyRent, String tenantHeader) {

        // Convert String IDs to UUID for database storage
        UUID propertyUuid = UUID.fromString(propertyId);
        UUID tenantUuid = UUID.fromString(tenantId);
        UUID landlordUuid = UUID.fromString(landlordId);

        Lease lease = Lease.builder()
                .id(UUID.randomUUID())
                .propertyId(propertyUuid)          // ✅ UUID
                .tenantId(tenantUuid)              // ✅ UUID
                .landlordId(landlordUuid)          // ✅ UUID
                .startDate(startDate)
                .endDate(endDate)
                .rentAmount(monthlyRent)           // ✅ field name is rentAmount, not monthlyRent
                .status(LeaseStatus.PENDING)
                .build();
        leaseRepository.save(lease);

        // Event DTO expects String, so convert back
        LeaseCreatedEvent event = new LeaseCreatedEvent(
                lease.getId().toString(),
                propertyUuid.toString(),           // ✅ String
                tenantUuid.toString(),             // ✅ String
                landlordUuid.toString(),           // ✅ String
                startDate,
                endDate,
                monthlyRent,
                UUID.randomUUID().toString()
        );
        eventPublisher.publishLeaseCreated(event, tenantHeader);
        log.info("Lease created and event published: {}", lease.getId());
        return lease;
    }

    @Transactional
    public Lease terminateLease(String leaseId, String reason, String tenantHeader) {
        UUID uuid = UUID.fromString(leaseId);
        Lease lease = leaseRepository.findById(uuid)
                .orElseThrow(() -> BusinessException.leaseNotFound(leaseId));

        if (lease.getStatus() == LeaseStatus.TERMINATED) {
            log.warn("Lease {} already terminated.", leaseId);
            return lease;
        }
        if (lease.getStatus() != LeaseStatus.ACTIVE) {
            throw BusinessException.invalidLeaseStatus(leaseId, lease.getStatus().name());
        }

        lease.setStatus(LeaseStatus.TERMINATED);
        lease.setTerminationDate(LocalDate.now());
        lease.setTerminationReason(reason);
        leaseRepository.save(lease);

        // Convert UUID to String for event
        LeaseTerminatedEvent event = new LeaseTerminatedEvent(
                lease.getId().toString(),
                lease.getPropertyId().toString(),   // ✅ UUID → String
                lease.getTenantId().toString(),     // ✅ UUID → String
                lease.getTerminationDate(),
                reason
        );
        eventPublisher.publishLeaseTerminated(event, tenantHeader);
        log.info("Lease terminated: {}", leaseId);
        return lease;
    }

    @Transactional
    public void voidLease(String leaseId, String reason, String tenantHeader) {
        UUID uuid = UUID.fromString(leaseId);
        Lease lease = leaseRepository.findById(uuid)
                .orElseThrow(() -> BusinessException.leaseNotFound(leaseId));

        if (lease.getStatus() != LeaseStatus.PENDING) {
            log.warn("Lease {} not in PENDING state, cannot void.", leaseId);
            return;
        }
        lease.setStatus(LeaseStatus.VOIDED);
        lease.setVoidReason(reason);
        leaseRepository.save(lease);
        log.info("Lease {} voided due to: {}", leaseId, reason);
    }

    @Transactional
    public void activateLease(String leaseId) {
        UUID uuid = UUID.fromString(leaseId);
        Lease lease = leaseRepository.findById(uuid)
                .orElseThrow(() -> BusinessException.leaseNotFound(leaseId));

        if (lease.getStatus() == LeaseStatus.ACTIVE) {
            log.warn("Lease {} already active.", leaseId);
            return;
        }
        if (lease.getStatus() != LeaseStatus.PENDING) {
            throw BusinessException.invalidLeaseStatus(leaseId, lease.getStatus().name());
        }
        lease.setStatus(LeaseStatus.ACTIVE);
        lease.setActivatedAt(LocalDateTime.now());
        leaseRepository.save(lease);
        log.info("Lease {} activated.", leaseId);
    }
}
