package com.apex.PropertyManagementService.DTOs.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class UnitCreateRequest {

    // Optional — backend auto-generates (A, B, C...) when missing
    private String unitNumber;

    @NotNull(message = "Property ID is required")
    private UUID propertyId;

    private Integer bedrooms;
    private Integer bathrooms;
    private BigDecimal squareFeet;
    private BigDecimal monthlyRent;
    private BigDecimal securityDeposit;
}