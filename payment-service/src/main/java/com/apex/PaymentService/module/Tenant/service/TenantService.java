package com.apex.PaymentService.module.Tenant.service;

import com.apex.PaymentService.module.Tenant.dto.request.TenantCreateDto;
import com.apex.PaymentService.module.Tenant.dto.request.TenantUpdateDto;
import com.apex.PaymentService.module.Tenant.dto.response.TenantResponseDto;
import com.apex.PaymentService.module.Tenant.enums.TenantStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TenantService {
    TenantResponseDto createTenant(TenantCreateDto dto);
    TenantResponseDto getTenant(UUID id);
    TenantResponseDto getTenantByEmail(String email);
    Page<TenantResponseDto> listTenants(TenantStatus status, Pageable pageable);
    TenantResponseDto updateTenant(UUID id, TenantUpdateDto dto);
    TenantResponseDto updateFull(UUID id, TenantCreateDto dto);
    void deleteTenant(UUID id);
    boolean exists(UUID id);
}