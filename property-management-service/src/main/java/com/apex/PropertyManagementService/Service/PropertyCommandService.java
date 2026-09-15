package com.apex.PropertyManagementService.Service;

import com.apex.PropertyManagementService.DTOs.request.PropertyCreateRequest;
import com.apex.PropertyManagementService.DTOs.request.PropertyUpdateRequest;
import com.apex.PropertyManagementService.DTOs.response.PropertyResponse;

import java.util.UUID;

public interface PropertyCommandService {

    PropertyResponse createProperty(PropertyCreateRequest request, String tenantHeader);

    PropertyResponse updateProperty(UUID id, PropertyUpdateRequest request, String tenantHeader);

    PropertyResponse updatePropertyStatus(UUID id, String status, String tenantHeader);

    void deleteProperty(UUID id, String tenantHeader);
}