package com.apex.PropertyManagementService.DTOs.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class UnitResponse {
    private UUID id;
    private String unitNumber;
    private UUID propertyId;
    private Integer bedrooms;
    private Integer bathrooms;
    private BigDecimal squareFeet;
    private BigDecimal monthlyRent;
    private BigDecimal securityDeposit;
    private String status;
    private String currentTenantId;
}