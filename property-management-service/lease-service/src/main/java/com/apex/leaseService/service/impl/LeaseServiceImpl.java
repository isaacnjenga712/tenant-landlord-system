package com.apex.leaseService.service.impl;

import com.apex.leaseService.dtos.LeaseRequest;
import com.apex.leaseService.dtos.LeaseResponse;
import com.apex.leaseService.entity.Lease;
import com.apex.leaseService.entity.LeaseStatus;
import com.apex.leaseService.entity.User;
import com.apex.leaseService.exception.ResourceNotFoundException;
import com.apex.leaseService.repository.LeaseRepository;
import com.apex.leaseService.repository.UserRepository;
import com.apex.leaseService.service.LeaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaseServiceImpl implements LeaseService {

    private final LeaseRepository leaseRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public LeaseResponse createLease(LeaseRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        Lease lease = Lease.builder()
                .unitId(request.getUnitId())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .rentAmount(request.getRentAmount())
                .securityDepositAmount(request.getSecurityDepositAmount())
                .status(LeaseStatus.DRAFT)
                .build();

        List<User> tenants = userRepository.findAllById(request.getTenantIds());
        if (tenants.size() != request.getTenantIds().size()) {
            throw new IllegalArgumentException("One or more tenant IDs are invalid");
        }
        lease.setTenants(Set.copyOf(tenants));

        Lease savedLease = leaseRepository.save(lease);
        return mapToResponse(savedLease);
    }

    @Override
    @Transactional(readOnly = true)
    public LeaseResponse getLeaseById(UUID id) {
        Lease lease = leaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lease not found with id: " + id));
        return mapToResponse(lease);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaseResponse> getAllLeases() {
        return leaseRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaseResponse> getLeasesByUnit(UUID unitId) {
        return leaseRepository.findByUnitId(unitId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaseResponse> getLeasesByTenant(UUID tenantId) {
        return leaseRepository.findByTenantId(tenantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LeaseResponse updateLease(UUID id, LeaseRequest request) {
        Lease existingLease = leaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lease not found with id: " + id));

        existingLease.setUnitId(request.getUnitId());
        existingLease.setStartDate(request.getStartDate());
        existingLease.setEndDate(request.getEndDate());
        existingLease.setRentAmount(request.getRentAmount());
        existingLease.setSecurityDepositAmount(request.getSecurityDepositAmount());

        List<User> tenants = userRepository.findAllById(request.getTenantIds());
        existingLease.setTenants(Set.copyOf(tenants));

        Lease updatedLease = leaseRepository.save(existingLease);
        return mapToResponse(updatedLease);
    }

    @Override
    @Transactional
    public void deleteLease(UUID id) {
        if (!leaseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Lease not found with id: " + id);
        }
        leaseRepository.deleteById(id);
    }

    @Override
    @Transactional
    public LeaseResponse updateLeaseStatus(UUID id, String status) {
        Lease lease = leaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lease not found with id: " + id));

        try {
            lease.setStatus(LeaseStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            String allowed = Arrays.toString(LeaseStatus.values());
            throw new IllegalArgumentException("Invalid lease status: '" + status + "'. Allowed values: " + allowed);
        }

        Lease updated = leaseRepository.save(lease);
        return mapToResponse(updated);
    }

    // --- Internal mapper ---
    private LeaseResponse mapToResponse(Lease lease) {
        // ✅ FIXED: use User::getUserId (not getId)
        Set<UUID> tenantIds = lease.getTenants().stream()
                .map(User::getUserId)   // ✅ Now matches your User entity
                .collect(Collectors.toSet());

        return LeaseResponse.builder()
                .leaseId(lease.getLeaseId())
                .unitId(lease.getUnitId())
                .startDate(lease.getStartDate())
                .endDate(lease.getEndDate())
                .rentAmount(lease.getRentAmount())
                .securityDepositAmount(lease.getSecurityDepositAmount())
                .status(lease.getStatus())
                .tenantIds(tenantIds)
                .build();
    }
}