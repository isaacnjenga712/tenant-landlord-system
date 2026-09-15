package com.apex.module.lease.event;

import com.apex.module.lease.dto.response.LeaseResponse;

public class LeaseEventPayload {
    private LeaseResponse lease;
    private String failureReason;

    public LeaseEventPayload() {}

    public LeaseEventPayload(LeaseResponse lease) {
        this.lease = lease;
    }

    public LeaseEventPayload(LeaseResponse lease, String failureReason) {
        this.lease = lease;
        this.failureReason = failureReason;
    }

    public LeaseResponse getLease() { return lease; }
    public void setLease(LeaseResponse lease) { this.lease = lease; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
}
