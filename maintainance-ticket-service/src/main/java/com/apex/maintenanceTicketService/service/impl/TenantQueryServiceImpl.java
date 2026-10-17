package com.apex.maintenanceTicketService.service.impl;

import com.apex.maintenanceTicketService.dto.response.TenantResponse;
import com.apex.maintenanceTicketService.exception.ResourceNotFoundException;
import com.apex.maintenanceTicketService.mapper.TenantMapper;
import com.apex.maintenanceTicketService.model.Tenant;
import com.apex.maintenanceTicketService.repository.TenantRepository;
import com.apex.maintenanceTicketService.service.TenantQueryService;
import com.platform.common.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantQueryServiceImpl implements TenantQueryService {

    private final TenantRepository tenantRepository;
    private final TenantMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<TenantResponse> getAllTenants() {
        UUID orgId = TenantContext.require();
        // Uses the compound index — list of all tenants in the org
        return tenantRepository.findAll().stream()
                .filter(t -> orgId.equals(t.getOrgId()))
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TenantResponse getTenantById(UUID id) {
        UUID orgId = TenantContext.require();
        Tenant tenant = tenantRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + id));
        return mapper.toResponse(tenant);
    }

    @Override
    @Transactional(readOnly = true)
    public TenantResponse getTenantByEmail(String email) {
        UUID orgId = TenantContext.require();
        Tenant tenant = tenantRepository.findByOrgIdAndEmail(orgId, email)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with email: " + email));
        return mapper.toResponse(tenant);
    }
}