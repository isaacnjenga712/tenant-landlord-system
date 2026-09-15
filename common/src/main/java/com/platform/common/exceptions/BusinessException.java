package com.platform.common.exceptions;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final String code;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public static BusinessException leaseNotFound(String leaseId) {
        return new BusinessException("LEASE_001", "Lease not found with ID: " + leaseId);
    }

    public static BusinessException invalidLeaseStatus(String leaseId, String currentStatus) {
        return new BusinessException("LEASE_002",
                "Cannot perform action on lease " + leaseId + " with current status: " + currentStatus);
    }

    public static BusinessException paymentProcessingFailed(String transactionId) {
        return new BusinessException("PAYMENT_001",
                "Payment processing failed for transaction: " + transactionId);
    }
}
