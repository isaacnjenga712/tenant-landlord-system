package com.apex.leaseService.service;

import com.apex.leaseService.entity.Lease;
import com.apex.leaseService.repository.LeaseRepository;
import com.platform.common.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeaseQueryService {

    private final LeaseRepository leaseRepository;

    /**
     * Retrieve a lease by its ID (UUID as a String).
     * @param leaseId the lease ID in String format
     * @return the Lease entity
     * @throws BusinessException if the lease is not found
     */
    public Lease getLease(String leaseId) {
        UUID uuid = UUID.fromString(leaseId);
        return leaseRepository.findById(uuid)
                .orElseThrow(() -> BusinessException.leaseNotFound(leaseId));
    }

    /**
     * Retrieve all leases for a specific tenant.
     * @param tenantId the tenant's ID
     * @return list of leases (may be empty)
     */
    public List<Lease> getLeasesByTenant(UUID tenantId) {
        return leaseRepository.findByTenantId(tenantId);
    }

    /**
     * Retrieve all leases for a specific property.
     * @param propertyId the property's ID
     * @return list of leases (may be empty)
     */
    public List<Lease> getLeasesByProperty(UUID propertyId) {
        return leaseRepository.findByPropertyId(propertyId);
    }

    /**
     * Retrieve all leases (use with caution for large datasets).
     * @return all leases
     */
    public List<Lease> getAllLeases() {
        return leaseRepository.findAll();
    }
}
