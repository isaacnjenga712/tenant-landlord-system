package com.apex.module.lease.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LeaseUpdateRequest {
    private LocalDate startDate;
    private LocalDate endDate;
    @Positive private BigDecimal rentAmount;
    @PositiveOrZero private BigDecimal depositAmount;
    private String termsAndConditions;

    public LeaseUpdateRequest() {}

    public LeaseUpdateRequest(LocalDate startDate, LocalDate endDate,
                              BigDecimal rentAmount, BigDecimal depositAmount,
                              String termsAndConditions) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.rentAmount = rentAmount;
        this.depositAmount = depositAmount;
        this.termsAndConditions = termsAndConditions;
    }

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