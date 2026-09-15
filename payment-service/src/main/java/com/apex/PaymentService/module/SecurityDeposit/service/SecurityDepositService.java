package com.apex.PaymentService.module.SecurityDeposit.service;

import com.apex.PaymentService.module.SecurityDeposit.dto.request.DeductionRequestDto;
import com.apex.PaymentService.module.SecurityDeposit.dto.request.ReturnDepositRequestDto;
import com.apex.PaymentService.module.SecurityDeposit.dto.request.SecurityDepositCreateDto;
import com.apex.PaymentService.module.SecurityDeposit.dto.request.SecurityDepositUpdateDto;
import com.apex.PaymentService.module.SecurityDeposit.dto.response.SecurityDepositResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

public interface SecurityDepositService {

    SecurityDepositResponseDto createDeposit(SecurityDepositCreateDto dto);

    SecurityDepositResponseDto getDeposit(UUID id);

    SecurityDepositResponseDto getDepositByLease(UUID leaseId);

    Page<SecurityDepositResponseDto> listDeposits(UUID leaseId, Boolean activeOnly, Pageable pageable);

    SecurityDepositResponseDto updateDeposit(UUID id, SecurityDepositUpdateDto dto);

    void deleteDeposit(UUID id);

    SecurityDepositResponseDto addDeduction(UUID id, DeductionRequestDto dto);

    SecurityDepositResponseDto returnDeposit(UUID id, ReturnDepositRequestDto dto);

    void accrueInterest(UUID id, BigDecimal interestRate);
    
    boolean exists(UUID depositId);
    boolean isActive(UUID depositId);
    BigDecimal getCurrentBalance(UUID depositId);
    void applyDeduction(UUID depositId, BigDecimal amount);
    void reverseDeduction(UUID depositId, BigDecimal amount);
}
