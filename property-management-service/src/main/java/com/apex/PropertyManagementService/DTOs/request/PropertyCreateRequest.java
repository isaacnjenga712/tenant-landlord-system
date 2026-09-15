package com.apex.PropertyManagementService.DTOs.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class PropertyCreateRequest {
    @NotNull(message = "Property ID is required")
    private UUID propertyId;          // ✅ UUID

    @NotNull(message = "Landlord ID is required")
    private UUID landlordId;          // ✅ UUID

    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}