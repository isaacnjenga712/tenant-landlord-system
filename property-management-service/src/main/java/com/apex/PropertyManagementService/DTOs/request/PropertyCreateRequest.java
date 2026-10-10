package com.apex.PropertyManagementService.DTOs.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class PropertyCreateRequest {

    @NotNull(message = "Property ID is required")
    private UUID propertyId;

    // landlordId no longer required — backend derives from JWT / X-User-Id header
    private UUID landlordId;

    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}