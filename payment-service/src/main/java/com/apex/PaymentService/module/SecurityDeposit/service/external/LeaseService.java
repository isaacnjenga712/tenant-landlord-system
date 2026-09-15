package com.apex.PaymentService.module.SecurityDeposit.service.external;

import java.util.UUID;

public interface LeaseService {
    boolean exists(UUID leaseId);
}
