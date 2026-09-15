package com.apex.maintenanceTicketService.mapper;

import com.apex.maintenanceTicketService.dto.request.TenantCreateRequest;
import com.apex.maintenanceTicketService.dto.request.TenantUpdateRequest;
import com.apex.maintenanceTicketService.dto.response.TenantResponse;
import com.apex.maintenanceTicketService.model.Tenant;
import org.springframework.stereotype.Component;

@Component
public class TenantMapper {

    public Tenant toEntity(TenantCreateRequest request) {
        return Tenant.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .build();
    }

    public TenantResponse toResponse(Tenant tenant) {
        return TenantResponse.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .email(tenant.getEmail())
                .phone(tenant.getPhone())
                .createdAt(tenant.getCreatedAt())
                .build();
    }

    public void updateEntity(Tenant tenant, TenantUpdateRequest request) {
        if (request.getName() != null) tenant.setName(request.getName());
        if (request.getEmail() != null) tenant.setEmail(request.getEmail());
        if (request.getPhone() != null) tenant.setPhone(request.getPhone());
    }
}