package com.apex.maintenanceTicketService.service.impl;

import com.apex.maintenanceTicketService.dto.request.TenantCreateRequest;
import com.apex.maintenanceTicketService.dto.request.TenantUpdateRequest;
import com.apex.maintenanceTicketService.dto.response.TenantResponse;
import com.apex.maintenanceTicketService.exception.ResourceNotFoundException;
import com.apex.maintenanceTicketService.mapper.TenantMapper;
import com.apex.maintenanceTicketService.model.Tenant;
import com.apex.maintenanceTicketService.repository.TenantRepository;
import com.apex.maintenanceTicketService.service.TenantCommandService;
import com.platform.common.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantCommandServiceImpl implements TenantCommandService {

    private final TenantRepository tenantRepository;
    private final TenantMapper mapper;

    @Override
    @Transactional
    public TenantResponse createTenant(TenantCreateRequest request) {
        UUID orgId = TenantContext.require();

        if (tenantRepository.existsByOrgIdAndEmail(orgId, request.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }

        Tenant tenant = mapper.toEntity(request);
        tenant.setOrgId(orgId);
        Tenant saved = tenantRepository.save(tenant);
        log.info("Tenant created with id: {} org={}", saved.getId(), orgId);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TenantResponse updateTenant(UUID id, TenantUpdateRequest request) {
        UUID orgId = TenantContext.require();
        Tenant tenant = findOrThrow(id, orgId);
        if (request.getEmail() != null) {
            tenantRepository.findByOrgIdAndEmail(orgId, request.getEmail())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            throw new IllegalArgumentException("Email already taken by another tenant");
                        }
                    });
        }
        mapper.updateEntity(tenant, request);
        Tenant updated = tenantRepository.save(tenant);
        log.info("Tenant updated: {}", updated.getId());
        return mapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteTenant(UUID id) {
        UUID orgId = TenantContext.require();
        Tenant tenant = findOrThrow(id, orgId);
        tenantRepository.delete(tenant);
        log.info("Tenant deleted: {}", id);
    }

    private Tenant findOrThrow(UUID id, UUID orgId) {
        return tenantRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + id));
    }
}