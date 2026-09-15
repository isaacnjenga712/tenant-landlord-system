package com.apex.PaymentService.module.SecurityDeposit.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class SecurityDepositUpdateDto {
    private UUID heldInAccountId;
    // other fields could be added, but for now we allow only changing the account
}
