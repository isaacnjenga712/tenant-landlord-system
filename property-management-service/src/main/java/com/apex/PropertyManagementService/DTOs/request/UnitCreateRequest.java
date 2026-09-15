package com.apex.PropertyManagementService.DTOs.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class UnitCreateRequest {
    @NotBlank
    private String unitNumber;

    @NotNull
    private UUID propertyId;

    private Integer bedrooms;
    private Integer bathrooms;
    private BigDecimal squareFeet;
    private BigDecimal monthlyRent;
    private BigDecimal securityDeposit;
}