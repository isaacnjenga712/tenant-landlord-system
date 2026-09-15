package com.apex.PaymentService.module.SecurityDeposit.service.external;

import java.util.UUID;

public interface PaymentAccountService {
    boolean exists(UUID accountId);
}