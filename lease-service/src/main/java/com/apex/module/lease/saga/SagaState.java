package com.apex.module.lease.saga;

import com.apex.module.lease.enums.SagaStatus;

import java.util.UUID;

public class SagaState {
    private UUID leaseId;
    private SagaStatus status;
    private SagaStep currentStep;
    private String errorMessage;

    // No-args constructor
    public SagaState() {
    }

    // All-args constructor
    public SagaState(UUID leaseId, SagaStatus status, SagaStep currentStep, String errorMessage) {
        this.leaseId = leaseId;
        this.status = status;
        this.currentStep = currentStep;
        this.errorMessage = errorMessage;
    }

    // Getters and Setters
    public UUID getLeaseId() {
        return leaseId;
    }

    public void setLeaseId(UUID leaseId) {
        this.leaseId = leaseId;
    }

    public SagaStatus getStatus() {
        return status;
    }

    public void setStatus(SagaStatus status) {
        this.status = status;
    }

    public SagaStep getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(SagaStep currentStep) {
        this.currentStep = currentStep;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
