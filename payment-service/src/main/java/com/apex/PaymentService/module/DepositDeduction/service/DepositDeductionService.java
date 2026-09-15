package com.apex.PaymentService.module.DepositDeduction.service;

import com.apex.PaymentService.module.DepositDeduction.dto.request.DepositDeductionCreateDto;
import com.apex.PaymentService.module.DepositDeduction.dto.request.DepositDeductionUpdateDto;
import com.apex.PaymentService.module.DepositDeduction.dto.response.DepositDeductionResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface DepositDeductionService {

    DepositDeductionResponseDto createDeduction(DepositDeductionCreateDto dto);

    DepositDeductionResponseDto getDeduction(UUID id);

    List<DepositDeductionResponseDto> getDeductionsByDeposit(UUID depositId);

    Page<DepositDeductionResponseDto> getDeductionsByDepositPaged(UUID depositId, Pageable pageable);

    DepositDeductionResponseDto updateDeduction(UUID id, DepositDeductionUpdateDto dto);

    void deleteDeduction(UUID id);

    void deleteAllDeductionsByDeposit(UUID depositId);
}
