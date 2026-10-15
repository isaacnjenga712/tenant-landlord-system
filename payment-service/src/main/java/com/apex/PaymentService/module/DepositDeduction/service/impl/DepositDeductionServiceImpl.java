package com.apex.PaymentService.module.DepositDeduction.service.impl;

import com.apex.PaymentService.common.exception.BusinessException;
import com.apex.PaymentService.common.exception.ResourceNotFoundException;
import com.apex.PaymentService.module.DepositDeduction.dto.request.DepositDeductionCreateDto;
import com.apex.PaymentService.module.DepositDeduction.dto.request.DepositDeductionUpdateDto;
import com.apex.PaymentService.module.DepositDeduction.dto.response.DepositDeductionResponseDto;
import com.apex.PaymentService.module.DepositDeduction.entity.DepositDeduction;
import com.apex.PaymentService.module.DepositDeduction.mapper.DepositDeductionMapper;
import com.apex.PaymentService.module.DepositDeduction.repository.DepositDeductionRepository;
import com.apex.PaymentService.module.DepositDeduction.service.DepositDeductionService;
import com.apex.PaymentService.module.SecurityDeposit.service.SecurityDepositService;
import com.platform.common.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepositDeductionServiceImpl implements DepositDeductionService {

    private final DepositDeductionRepository repository;
    private final DepositDeductionMapper mapper;
    private final SecurityDepositService securityDepositService;

    @Override
    @Transactional
    public DepositDeductionResponseDto createDeduction(DepositDeductionCreateDto dto) {
        UUID orgId = TenantContext.require();

        if (!securityDepositService.exists(dto.getDepositId())) {
            throw new ResourceNotFoundException("Security deposit not found with id: " + dto.getDepositId());
        }
        if (!securityDepositService.isActive(dto.getDepositId())) {
            throw new BusinessException("Cannot add deduction to a returned deposit");
        }

        BigDecimal currentBalance = securityDepositService.getCurrentBalance(dto.getDepositId());
        BigDecimal existingTotal = repository.findByOrgIdAndDepositId(orgId, dto.getDepositId())
                .stream()
                .map(DepositDeduction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal newTotal = existingTotal.add(dto.getAmount());
        if (newTotal.compareTo(currentBalance) > 0) {
            throw new BusinessException("Total deductions (" + newTotal +
                    ") exceed current balance (" + currentBalance + ")");
        }

        DepositDeduction deduction = mapper.toEntity(dto);
        deduction.setOrgId(orgId);
        DepositDeduction saved = repository.save(deduction);
        log.info("Created deduction for deposit {} org {}", saved.getDepositId(), orgId);

        securityDepositService.applyDeduction(dto.getDepositId(), dto.getAmount());

        return mapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DepositDeductionResponseDto getDeduction(UUID id) {
        UUID orgId = TenantContext.require();
        DepositDeduction deduction = repository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Deduction not found"));
        return mapper.toResponseDto(deduction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepositDeductionResponseDto> getDeductionsByDeposit(UUID depositId) {
        UUID orgId = TenantContext.require();
        return repository.findByOrgIdAndDepositId(orgId, depositId)
                .stream()
                .map(mapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DepositDeductionResponseDto> getDeductionsByDepositPaged(UUID depositId, Pageable pageable) {
        UUID orgId = TenantContext.require();
        return repository.findByOrgIdAndDepositId(orgId, depositId, pageable).map(mapper::toResponseDto);
    }

    @Override
    @Transactional
    public DepositDeductionResponseDto updateDeduction(UUID id, DepositDeductionUpdateDto dto) {
        UUID orgId = TenantContext.require();
        DepositDeduction deduction = repository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Deduction not found"));

        if (dto.getAmount() != null && !dto.getAmount().equals(deduction.getAmount())) {
            BigDecimal currentBalance = securityDepositService.getCurrentBalance(deduction.getDepositId());
            BigDecimal otherDeductions = repository.findByOrgIdAndDepositId(orgId, deduction.getDepositId())
                    .stream()
                    .filter(d -> !d.getId().equals(id))
                    .map(DepositDeduction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal newTotal = otherDeductions.add(dto.getAmount());
            if (newTotal.compareTo(currentBalance) > 0) {
                throw new BusinessException("Total deductions after update (" + newTotal +
                        ") exceed current balance (" + currentBalance + ")");
            }
            BigDecimal oldAmount = deduction.getAmount();
            securityDepositService.reverseDeduction(deduction.getDepositId(), oldAmount);
            securityDepositService.applyDeduction(deduction.getDepositId(), dto.getAmount());
        }

        mapper.updateEntity(dto, deduction);
        DepositDeduction updated = repository.save(deduction);
        log.info("Updated deduction {}", updated.getId());
        return mapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public void deleteDeduction(UUID id) {
        UUID orgId = TenantContext.require();
        DepositDeduction deduction = repository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Deduction not found"));
        securityDepositService.reverseDeduction(deduction.getDepositId(), deduction.getAmount());
        repository.deleteById(id);
        log.info("Deleted deduction {}", id);
    }

    @Override
    @Transactional
    public void deleteAllDeductionsByDeposit(UUID depositId) {
        UUID orgId = TenantContext.require();
        if (!securityDepositService.exists(depositId)) {
            throw new ResourceNotFoundException("Deposit not found");
        }
        List<DepositDeduction> deductions = repository.findByOrgIdAndDepositId(orgId, depositId);
        BigDecimal total = deductions.stream()
                .map(DepositDeduction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (total.compareTo(BigDecimal.ZERO) > 0) {
            securityDepositService.reverseDeduction(depositId, total);
        }
        repository.deleteByOrgIdAndDepositId(orgId, depositId);
        log.info("Deleted all deductions for deposit {} org {}", depositId, orgId);
    }
}