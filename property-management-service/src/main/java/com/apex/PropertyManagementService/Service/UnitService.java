package com.apex.PropertyManagementService.Service;

import com.apex.PropertyManagementService.DTOs.request.UnitCreateRequest;
import com.apex.PropertyManagementService.DTOs.request.UnitUpdateRequest;
import com.apex.PropertyManagementService.DTOs.response.UnitResponse;

import java.util.List;
import java.util.UUID;

public interface UnitService {

    UnitResponse createUnit(UnitCreateRequest request, String tenantHeader);

    List<UnitResponse> getAllUnits();

    UnitResponse getUnitById(UUID id);

    UnitResponse getUnitByUnitNumber(String unitNumber);

    List<UnitResponse> getUnitsByProperty(UUID propertyId);

    List<UnitResponse> getAvailableUnitsByProperty(UUID propertyId);

    List<UnitResponse> getUnitsByStatus(String status);

    List<UnitResponse> getUnitsByTenant(UUID tenantId);

    UnitResponse updateUnit(UUID id, UnitUpdateRequest request, String tenantHeader);

    UnitResponse updateUnitStatus(UUID id, String status, String tenantHeader);

    // ✅ Correct signature: id (UUID), tenantId (UUID), tenantHeader (String)
    UnitResponse assignTenant(UUID id, UUID tenantId, String tenantHeader);

    UnitResponse vacateUnit(UUID id, String tenantHeader);

    void deleteUnit(UUID id, String tenantHeader);
}