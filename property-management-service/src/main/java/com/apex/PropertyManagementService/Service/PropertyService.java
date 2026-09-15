package com.apex.PropertyManagementService.Service;

import com.apex.PropertyManagementService.DTOs.request.PropertyCreateRequest;
import com.apex.PropertyManagementService.DTOs.request.PropertyUpdateRequest;
import com.apex.PropertyManagementService.DTOs.response.PropertyResponse;

import java.util.List;
import java.util.UUID;

public interface PropertyService {

    PropertyResponse createProperty(PropertyCreateRequest request, String tenantHeader);

    List<PropertyResponse> getAllProperties();

    PropertyResponse getPropertyById(UUID id);

    PropertyResponse getPropertyByPropertyId(String propertyId);

    List<PropertyResponse> getPropertiesByLandlord(UUID landlordId);

    List<PropertyResponse> getPropertiesByStatus(String status);

    List<PropertyResponse> getPropertiesByCity(String city);

    PropertyResponse updateProperty(UUID id, PropertyUpdateRequest request, String tenantHeader);

    PropertyResponse updatePropertyStatus(UUID id, String status, String tenantHeader);

    void deleteProperty(UUID id, String tenantHeader);
}