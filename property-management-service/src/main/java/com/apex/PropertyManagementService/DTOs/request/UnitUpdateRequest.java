package com.apex.PropertyManagementService.DTOs.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UnitUpdateRequest {
    private String unitNumber;
    private Integer bedrooms;
    private Integer bathrooms;
    private BigDecimal squareFeet;
    private BigDecimal monthlyRent;
    private BigDecimal securityDeposit;
    private String status;
}