package com.apex.PropertyManagementService.Service.impl;

import com.apex.PropertyManagementService.DTOs.request.LandlordCreateRequest;
import com.apex.PropertyManagementService.DTOs.request.LandlordUpdateRequest;
import com.apex.PropertyManagementService.DTOs.response.LandlordResponse;
import com.apex.PropertyManagementService.entity.Landlord;
import com.apex.PropertyManagementService.entity.enums.LandlordStatus;
import com.apex.PropertyManagementService.exception.ResourceNotFoundException;
import com.apex.PropertyManagementService.mapper.LandlordMapper;
import com.apex.PropertyManagementService.repository.LandlordRepository;
import com.apex.PropertyManagementService.Service.LandlordService;
import com.platform.common.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LandlordServiceImpl implements LandlordService {

    private final LandlordRepository landlordRepository;
    private final LandlordMapper mapper;

    @Override
    @Transactional
    public LandlordResponse createLandlord(LandlordCreateRequest request) {
        UUID orgId = TenantContext.require();

        landlordRepository.findByEmailAndOrgId(request.getEmail(), orgId)
                .ifPresent(l -> { throw new IllegalArgumentException("Email already registered"); });

        Landlord landlord = mapper.toEntity(request);
        landlord.setOrgId(orgId);
        landlord.setStatus(LandlordStatus.ACTIVE);
        Landlord saved = landlordRepository.save(landlord);
        log.info("Landlord created with id: {} org={}", saved.getId(), orgId);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LandlordResponse> getAllLandlords() {
        UUID orgId = TenantContext.require();
        return mapper.toResponseList(landlordRepository.findByOrgId(orgId));
    }

    @Override
    @Transactional(readOnly = true)
    public LandlordResponse getLandlordById(UUID id) {
        Landlord landlord = findOrThrow(id);
        return mapper.toResponse(landlord);
    }

    @Override
    @Transactional(readOnly = true)
    public LandlordResponse getLandlordByEmail(String email) {
        UUID orgId = TenantContext.require();
        Landlord landlord = landlordRepository.findByEmailAndOrgId(email, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Landlord not found with email: " + email));
        return mapper.toResponse(landlord);
    }

    @Override
    @Transactional
    public LandlordResponse updateLandlord(UUID id, LandlordUpdateRequest request) {
        UUID orgId = TenantContext.require();
        Landlord landlord = findOrThrow(id);

        if (request.getEmail() != null) {
            landlordRepository.findByEmailAndOrgId(request.getEmail(), orgId)
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            throw new IllegalArgumentException("Email already taken by another landlord");
                        }
                    });
        }
        mapper.updateEntity(landlord, request);
        if (request.getStatus() != null) {
            try {
                landlord.setStatus(LandlordStatus.valueOf(request.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status: " + request.getStatus());
            }
        }
        Landlord updated = landlordRepository.save(landlord);
        log.info("Landlord updated: {}", updated.getId());
        return mapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteLandlord(UUID id) {
        Landlord landlord = findOrThrow(id);
        if (!landlord.getProperties().isEmpty()) {
            throw new IllegalStateException("Cannot delete landlord with properties. Deactivate instead.");
        }
        landlordRepository.delete(landlord);
        log.info("Landlord deleted: {}", id);
    }

    private Landlord findOrThrow(UUID id) {
        UUID orgId = TenantContext.require();
        return landlordRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Landlord not found with id: " + id));
    }
}