package com.apex.PaymentService.module.PaymentAccount.service;

import com.apex.PaymentService.module.PaymentAccount.dto.request.BalanceAdjustmentDto;
import com.apex.PaymentService.module.PaymentAccount.dto.request.PaymentAccountCreateDto;
import com.apex.PaymentService.module.PaymentAccount.dto.request.PaymentAccountUpdateDto;
import com.apex.PaymentService.module.PaymentAccount.dto.request.TransferRequestDto;
import com.apex.PaymentService.module.PaymentAccount.dto.response.PaymentAccountResponseDto;
import com.apex.PaymentService.module.PaymentAccount.enums.EntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PaymentAccountService {

    PaymentAccountResponseDto createAccount(PaymentAccountCreateDto dto);

    PaymentAccountResponseDto getAccount(UUID id);

    PaymentAccountResponseDto getAccountByEntity(EntityType entityType, UUID entityId);

    Page<PaymentAccountResponseDto> listAccounts(EntityType entityType, Pageable pageable);

    PaymentAccountResponseDto updateAccount(UUID id, PaymentAccountUpdateDto dto);

    void deleteAccount(UUID id);

    PaymentAccountResponseDto adjustBalance(UUID id, BalanceAdjustmentDto dto);

    PaymentAccountResponseDto transferBalance(UUID id, TransferRequestDto dto);

    boolean exists(UUID id);
}