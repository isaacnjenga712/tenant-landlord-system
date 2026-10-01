package com.apex.PaymentService.module.Invoice.service.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

/**
 * Calls lease-service over HTTP to resolve tenant + landlord for a lease.
 */
@Component
@Slf4j
public class LeaseValidationService {

    private final RestTemplate restTemplate;
    private final String leaseServiceUrl;

    public LeaseValidationService(RestTemplateBuilder builder,
                                  @Value("${services.lease.url:http://lease-service:8081}") String leaseServiceUrl) {
        this.restTemplate = builder.build();
        this.leaseServiceUrl = leaseServiceUrl;
    }

    public record LeaseInfo(UUID tenantId, UUID landlordId, UUID propertyId) {}

    public boolean exists(UUID leaseId) {
        try {
            getLease(leaseId);
            return true;
        } catch (Exception e) {
            log.warn("Lease {} lookup failed: {}", leaseId, e.getMessage());
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public LeaseInfo getLease(UUID leaseId) {
        String url = leaseServiceUrl + "/api/v1/leases/" + leaseId;
        log.debug("Fetching lease from {}", url);

        Map<String, Object> body = restTemplate.getForObject(url, Map.class);
        if (body == null) {
            throw new IllegalStateException("Empty response for lease " + leaseId);
        }

        UUID tenantId = parseUuid(body.get("tenantId"));
        UUID landlordId = parseUuid(body.get("landlordId"));
        UUID propertyId = parseUuid(body.get("propertyId"));

        return new LeaseInfo(tenantId, landlordId, propertyId);
    }

    private UUID parseUuid(Object v) {
        if (v == null) return null;
        try {
            return UUID.fromString(v.toString());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
