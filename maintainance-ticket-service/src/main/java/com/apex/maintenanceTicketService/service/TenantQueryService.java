package com.apex.maintenanceTicketService.service;

import com.apex.maintenanceTicketService.dto.response.TenantResponse;

import java.util.List;
import java.util.UUID;

public interface TenantQueryService {

    List<TenantResponse> getAllTenants();

    TenantResponse getTenantById(UUID id);

    TenantResponse getTenantByEmail(String email);
}