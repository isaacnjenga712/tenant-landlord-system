package com.apex.module.lease.service.impl;

import com.apex.module.lease.dto.request.LeaseCreateRequest;
import com.apex.module.lease.dto.request.LeaseUpdateRequest;
import com.apex.module.lease.dto.response.LeaseListResponse;
import com.apex.module.lease.dto.response.LeaseResponse;
import com.apex.module.lease.enums.LeaseStatus;
import com.apex.module.lease.event.SagaReplyEvent;
import com.apex.module.lease.exception.*;
import com.apex.module.lease.mapper.LeaseMapper;
import com.apex.module.lease.entity.Lease;
import com.apex.module.lease.repository.LeaseRepository;
import com.apex.module.lease.service.LeaseService;
import com.apex.module.lease.service.producer.LeaseEventProducer;
import com.apex.module.lease.specification.LeaseSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class LeaseServiceImpl implements LeaseService {

    private final LeaseRepository leaseRepository;
    private final LeaseMapper leaseMapper;
    private final LeaseEventProducer leaseEventProducer;

    // Explicit constructor (replaces @RequiredArgsConstructor)
    public LeaseServiceImpl(LeaseRepository leaseRepository,
                            LeaseMapper leaseMapper,
                            LeaseEventProducer leaseEventProducer) {
        this.leaseRepository = leaseRepository;
        this.leaseMapper = leaseMapper;
        this.leaseEventProducer = leaseEventProducer;
    }

    @Override
    public LeaseResponse createLease(LeaseCreateRequest request) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new LeaseValidationException("Start date must be before or equal to end date");
        }

        List<Lease> overlapping = leaseRepository.findOverlappingLeases(
                request.getPropertyId(),
                request.getStartDate(),
                request.getEndDate()
        );
        if (!overlapping.isEmpty()) {
            throw new LeaseOverlapException("Property already leased for the given period");
        }

        Lease lease = leaseMapper.toEntity(request);
        lease.setStatus(LeaseStatus.DRAFT);
        Lease saved = leaseRepository.save(lease);
        LeaseResponse response = leaseMapper.toResponse(saved);

        leaseEventProducer.publishLeaseCreationRequested(response);
        return response;
    }

    @Override
    public LeaseResponse getLease(UUID id) {
        Lease lease = leaseRepository.findById(id)
                .orElseThrow(() -> new LeaseNotFoundException("Lease not found with id: " + id));
        return leaseMapper.toResponse(lease);
    }

    @Override
    public LeaseResponse updateLease(UUID id, LeaseUpdateRequest request) {
        Lease lease = leaseRepository.findById(id)
                .orElseThrow(() -> new LeaseNotFoundException("Lease not found with id: " + id));

        if (lease.getStatus() == LeaseStatus.TERMINATED || lease.getStatus() == LeaseStatus.EXPIRED) {
            throw new LeaseUpdateNotAllowedException("Cannot update a terminated or expired lease");
        }

        if (request.getStartDate() != null) lease.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) lease.setEndDate(request.getEndDate());
        if (request.getRentAmount() != null) lease.setRentAmount(request.getRentAmount());
        if (request.getDepositAmount() != null) lease.setDepositAmount(request.getDepositAmount());
        if (request.getTermsAndConditions() != null) lease.setTermsAndConditions(request.getTermsAndConditions());

        if (request.getStartDate() != null || request.getEndDate() != null) {
            LocalDate newStart = lease.getStartDate();
            LocalDate newEnd = lease.getEndDate();
            if (newStart.isAfter(newEnd)) {
                throw new LeaseValidationException("Start date must be before or equal to end date");
            }
            List<Lease> overlapping = leaseRepository.findOverlappingLeases(
                    lease.getPropertyId(), newStart, newEnd);
            overlapping.removeIf(l -> l.getId().equals(lease.getId()));
            if (!overlapping.isEmpty()) {
                throw new LeaseOverlapException("Updated dates overlap with another lease");
            }
        }

        Lease updated = leaseRepository.save(lease);
        return leaseMapper.toResponse(updated);
    }

    @Override
    public void cancelLease(UUID id) {
        Lease lease = leaseRepository.findById(id)
                .orElseThrow(() -> new LeaseNotFoundException("Lease not found"));
        if (lease.getStatus() != LeaseStatus.DRAFT) {
            throw new LeaseCancellationException("Only DRAFT leases can be cancelled");
        }
        lease.setStatus(LeaseStatus.CANCELLED);
        leaseRepository.save(lease);
    }

    @Override
    public void terminateLease(UUID id, LocalDate terminationDate) {
        Lease lease = leaseRepository.findById(id)
                .orElseThrow(() -> new LeaseNotFoundException("Lease not found"));
        if (lease.getStatus() != LeaseStatus.ACTIVE) {
            throw new LeaseTerminationException("Only ACTIVE leases can be terminated");
        }
        if (terminationDate.isBefore(lease.getStartDate()) || terminationDate.isAfter(lease.getEndDate())) {
            throw new LeaseTerminationException("Termination date must be between start and end date");
        }
        lease.setEndDate(terminationDate);
        lease.setStatus(LeaseStatus.TERMINATED);
        leaseRepository.save(lease);
    }

    @Override
    public LeaseResponse renewLease(UUID id, LocalDate newEndDate) {
        Lease lease = leaseRepository.findById(id)
                .orElseThrow(() -> new LeaseNotFoundException("Lease not found"));
        if (lease.getStatus() != LeaseStatus.ACTIVE) {
            throw new LeaseRenewalException("Only ACTIVE leases can be renewed");
        }
        if (newEndDate.isBefore(lease.getEndDate())) {
            throw new LeaseRenewalException("New end date must be after current end date");
        }

        List<Lease> overlapping = leaseRepository.findOverlappingLeases(
                lease.getPropertyId(), lease.getStartDate(), newEndDate);
        overlapping.removeIf(l -> l.getId().equals(lease.getId()));
        if (!overlapping.isEmpty()) {
            throw new LeaseOverlapException("Renewal period overlaps with another lease");
        }

        lease.setEndDate(newEndDate);
        Lease renewed = leaseRepository.save(lease);
        return leaseMapper.toResponse(renewed);
    }

    @Override
    public LeaseListResponse listLeases(UUID tenantId, UUID landlordId, UUID propertyId,
                                        LeaseStatus status, LocalDate startDate, LocalDate endDate,
                                        int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<Lease> spec = Specification
                .where(LeaseSpecification.hasTenantId(tenantId))
                .and(LeaseSpecification.hasLandlordId(landlordId))
                .and(LeaseSpecification.hasPropertyId(propertyId))
                .and(LeaseSpecification.hasStatus(status))
                .and(LeaseSpecification.hasStartDateAfterOrEqual(startDate))
                .and(LeaseSpecification.hasEndDateBeforeOrEqual(endDate));

        Page<Lease> leasePage = leaseRepository.findAll(spec, pageable);
        List<LeaseResponse> responses = leasePage.getContent().stream()
                .map(leaseMapper::toResponse)
                .toList();

        LeaseListResponse listResponse = new LeaseListResponse();
        listResponse.setLeases(responses);
        listResponse.setPage(leasePage.getNumber());
        listResponse.setSize(leasePage.getSize());
        listResponse.setTotalElements(leasePage.getTotalElements());
        listResponse.setTotalPages(leasePage.getTotalPages());
        return listResponse;
    }

    @Override
    @Transactional
    public void handleSagaReply(SagaReplyEvent reply) {
        Lease lease = leaseRepository.findById(reply.getLeaseId())
                .orElseThrow(() -> new LeaseNotFoundException("Lease not found with id: " + reply.getLeaseId()));

        if ("PROPERTY_RESERVATION".equals(reply.getStep())) {
            if (reply.isSuccess()) {
                LeaseResponse response = leaseMapper.toResponse(lease);
                leaseEventProducer.publishTenantValidationRequested(response);
            } else {
                lease.setStatus(LeaseStatus.CANCELLED);
                leaseRepository.save(lease);
                leaseEventProducer.publishLeaseCreationFailed(leaseMapper.toResponse(lease), reply.getMessage());
            }
        } else if ("TENANT_VALIDATION".equals(reply.getStep())) {
            if (reply.isSuccess()) {
                lease.setStatus(LeaseStatus.ACTIVE);
                leaseRepository.save(lease);
                leaseEventProducer.publishLeaseCreated(leaseMapper.toResponse(lease));
            } else {
                lease.setStatus(LeaseStatus.CANCELLED);
                leaseRepository.save(lease);
                leaseEventProducer.publishCancelPropertyReservation(leaseMapper.toResponse(lease));
                leaseEventProducer.publishLeaseCreationFailed(leaseMapper.toResponse(lease), reply.getMessage());
            }
        }
    }
}