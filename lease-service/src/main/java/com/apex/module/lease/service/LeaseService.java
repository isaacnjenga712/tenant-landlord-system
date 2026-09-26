package com.apex.module.lease.service;

import com.apex.module.lease.dto.request.LeaseCreateRequest;
import com.apex.module.lease.dto.response.LeaseListResponse;
import com.apex.module.lease.dto.response.LeaseResponse;
import com.apex.module.lease.dto.request.LeaseUpdateRequest;
import com.apex.module.lease.enums.LeaseStatus;
import com.apex.module.lease.event.SagaReplyEvent;

import java.time.LocalDate;
import java.util.UUID;

public interface LeaseService {

    LeaseResponse createLease(LeaseCreateRequest request);
    LeaseResponse getLease(UUID id);
    LeaseResponse updateLease(UUID id, LeaseUpdateRequest request);
    void cancelLease(UUID id);
    void terminateLease(UUID id, LocalDate terminationDate);
    LeaseResponse renewLease(UUID id, LocalDate newEndDate);

    LeaseResponse approveLease(UUID id);   // ← add

    LeaseListResponse listLeases(UUID tenantId, UUID landlordId, UUID propertyId,
                                 LeaseStatus status, LocalDate startDate, LocalDate endDate,
                                 int page, int size);

    void handleSagaReply(SagaReplyEvent reply);
}

