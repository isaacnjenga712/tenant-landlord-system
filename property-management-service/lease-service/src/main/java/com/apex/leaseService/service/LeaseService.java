package com.apex.leaseService.service;

import com.apex.leaseService.dtos.LeaseRequest;
import com.apex.leaseService.dtos.LeaseResponse;
import java.util.List;
import java.util.UUID;

public interface LeaseService {

    LeaseResponse createLease(LeaseRequest request);
    LeaseResponse getLeaseById(UUID id);
    List<LeaseResponse> getAllLeases();
    List<LeaseResponse> getLeasesByUnit(UUID unitId);
    List<LeaseResponse> getLeasesByTenant(UUID tenantId); 

    
    LeaseResponse updateLease(UUID id, LeaseRequest request);

    void deleteLease(UUID id);
    LeaseResponse updateLeaseStatus(UUID id, String status);
	
}