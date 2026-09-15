package com.apex.PaymentService.module.Payment.service.external;

import java.util.UUID;

public interface TenantValidationService {
    boolean exists(UUID tenantId);
}
