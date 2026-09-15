package com.apex.PropertyManagementService.DTOs.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class PropertyResponse {
    private UUID id;
    private UUID propertyId;
    private UUID landlordId;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private String status;
    private List<UUID> unitIds;
}