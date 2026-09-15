package com.apex.module.lease.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class LeaseCreateRequest {
    @NotNull private UUID propertyId;
    @NotNull private UUID tenantId;
    @NotNull private UUID landlordId;
    @NotNull @FutureOrPresent private LocalDate startDate;
    @NotNull @Future private LocalDate endDate;
    @NotNull @Positive private BigDecimal rentAmount;
    @PositiveOrZero private BigDecimal depositAmount;
    private String termsAndConditions;

    
    public LeaseCreateRequest() {}

    
    
    public LeaseCreateRequest(UUID propertyId, UUID tenantId, UUID landlordId,
                              LocalDate startDate, LocalDate endDate,
                              BigDecimal rentAmount, BigDecimal depositAmount,
                              String termsAndConditions) {
        this.propertyId = propertyId;
        this.tenantId = tenantId;
        this.landlordId = landlordId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.rentAmount = rentAmount;
        this.depositAmount = depositAmount;
        this.termsAndConditions = termsAndConditions;
    }

    // Getters and setters
    public UUID getPropertyId() { return propertyId; }
    public void setPropertyId(UUID propertyId) { this.propertyId = propertyId; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public UUID getLandlordId() { return landlordId; }
    public void setLandlordId(UUID landlordId) { this.landlordId = landlordId; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public BigDecimal getRentAmount() { return rentAmount; }
    public void setRentAmount(BigDecimal rentAmount) { this.rentAmount = rentAmount; }
    public BigDecimal getDepositAmount() { return depositAmount; }
    public void setDepositAmount(BigDecimal depositAmount) { this.depositAmount = depositAmount; }
    public String getTermsAndConditions() { return termsAndConditions; }
    public void setTermsAndConditions(String termsAndConditions) { this.termsAndConditions = termsAndConditions; }
}
