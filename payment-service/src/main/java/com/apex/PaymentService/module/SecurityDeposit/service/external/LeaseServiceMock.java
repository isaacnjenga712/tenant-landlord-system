package com.apex.PaymentService.module.SecurityDeposit.service.external;

import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class LeaseServiceMock implements LeaseService {
    @Override
    public boolean exists(UUID leaseId) {
        // TODO: Replace with real implementation (call Lease module/repository)
        return true;
    }
}
