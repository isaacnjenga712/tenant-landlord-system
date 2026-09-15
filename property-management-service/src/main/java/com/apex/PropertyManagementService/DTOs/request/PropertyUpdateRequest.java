package com.apex.PropertyManagementService.DTOs.request;

import lombok.Data;

import java.util.UUID;

@Data
public class PropertyUpdateRequest {
    private UUID propertyId;          // ✅ UUID
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private String status;
}