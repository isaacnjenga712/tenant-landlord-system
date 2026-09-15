package com.apex.module.lease.event;

import java.util.UUID;

public class SagaReplyEvent {
    private UUID leaseId;
    private String step;
    private boolean success;
    private String message;

    public SagaReplyEvent() {}

    public SagaReplyEvent(UUID leaseId, String step, boolean success, String message) {
        this.leaseId = leaseId;
        this.step = step;
        this.success = success;
        this.message = message;
    }

    public UUID getLeaseId() { return leaseId; }
    public void setLeaseId(UUID leaseId) { this.leaseId = leaseId; }
    public String getStep() { return step; }
    public void setStep(String step) { this.step = step; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}