package com.apex.PaymentService.module.Invoice.service.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mock service to validate lease existence.
 * In production, replace this with actual lease repository or Feign client.
 */
@Component
@Slf4j
public class LeaseValidationService {

    /**
     * Checks if a lease exists.
     * Currently always returns true (mock).
     *
     * @param leaseId the lease UUID to check
     * @return true if the lease exists, false otherwise
     */
    public boolean exists(UUID leaseId) {
        // TODO: Replace with actual lease repository call or microservice integration
        log.debug("Validating lease existence for id: {}", leaseId);
        // For development, assume all leases exist
        return true;
    }
}
