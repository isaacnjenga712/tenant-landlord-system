package com.apex.maintenanceTicketService.service;

import com.apex.maintenanceTicketService.dto.request.TenantCreateRequest;
import com.apex.maintenanceTicketService.dto.request.TenantUpdateRequest;
import com.apex.maintenanceTicketService.dto.response.TenantResponse;

import java.util.UUID;

public interface TenantCommandService {

    TenantResponse createTenant(TenantCreateRequest request);

    TenantResponse updateTenant(UUID id, TenantUpdateRequest request);

    void deleteTenant(UUID id);
}