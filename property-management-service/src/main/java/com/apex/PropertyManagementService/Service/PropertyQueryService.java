package com.apex.PropertyManagementService.Service;

import com.apex.PropertyManagementService.DTOs.response.PropertyResponse;

import java.util.List;
import java.util.UUID;

public interface PropertyQueryService {

    List<PropertyResponse> getAllProperties();
    PropertyResponse getPropertyById(UUID id);
    PropertyResponse getPropertyByPropertyId(UUID propertyId);   // ✅ changed to UUID
    List<PropertyResponse> getPropertiesByLandlord(UUID landlordId);
    List<PropertyResponse> getPropertiesByStatus(String status);
    List<PropertyResponse> getPropertiesByCity(String city);
}