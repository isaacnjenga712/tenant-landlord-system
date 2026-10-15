package com.apex.PaymentService.module.SecurityDeposit.service.impl;

import com.apex.PaymentService.common.exception.BusinessException;
import com.apex.PaymentService.common.exception.ResourceNotFoundException;
import com.apex.PaymentService.module.PaymentAccount.service.PaymentAccountService;
import com.apex.PaymentService.module.SecurityDeposit.dto.request.DeductionRequestDto;
import com.apex.PaymentService.module.SecurityDeposit.dto.request.ReturnDepositRequestDto;
import com.apex.PaymentService.module.SecurityDeposit.dto.request.SecurityDepositCreateDto;
import com.apex.PaymentService.module.SecurityDeposit.dto.request.SecurityDepositUpdateDto;
import com.apex.PaymentService.module.SecurityDeposit.dto.response.SecurityDepositResponseDto;
import com.apex.PaymentService.module.SecurityDeposit.entity.SecurityDeposit;
import com.apex.PaymentService.module.SecurityDeposit.mapper.SecurityDepositMapper;
import com.apex.PaymentService.module.SecurityDeposit.repository.SecurityDepositRepository;
import com.apex.PaymentService.module.SecurityDeposit.service.SecurityDepositService;
import com.apex.PaymentService.module.SecurityDeposit.service.external.LeaseService;
import com.platform.common.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityDepositServiceImpl implements SecurityDepositService {

    private final SecurityDepositRepository repository;
    private final SecurityDepositMapper mapper;
    private final LeaseService leaseService;
    private final PaymentAccountService paymentAccountService;

    @Override
    @Transactional
    public SecurityDepositResponseDto createDeposit(SecurityDepositCreateDto dto) {
        UUID orgId = TenantContext.require();

        if (!leaseService.exists(dto.getLeaseId())) {
            throw new ResourceNotFoundException("Lease not found: " + dto.getLeaseId());
        }
        if (repository.findByOrgIdAndLeaseId(orgId, dto.getLeaseId()).isPresent()) {
            throw new BusinessException("Deposit already exists for lease: " + dto.getLeaseId());
        }
        if (dto.getHeldInAccountId() != null && !paymentAccountService.exists(dto.getHeldInAccountId())) {
            throw new ResourceNotFoundException("Payment account not found: " + dto.getHeldInAccountId());
        }

        SecurityDeposit deposit = mapper.toEntity(dto);
        deposit.setOrgId(orgId);
        deposit.setCurrentBalance(dto.getTotalDeposit());
        deposit.setDeductionTotal(BigDecimal.ZERO);
        deposit.setInterestAccrued(BigDecimal.ZERO);

        SecurityDeposit saved = repository.save(deposit);
        log.info("Created security deposit for lease {} org {}", saved.getLeaseId(), orgId);
        return mapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SecurityDepositResponseDto getDeposit(UUID id) {
        UUID orgId = TenantContext.require();
        SecurityDeposit deposit = repository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Deposit not found"));
        return mapper.toResponseDto(deposit);
    }

    @Override
    @Transactional(readOnly = true)
    public SecurityDepositResponseDto getDepositByLease(UUID leaseId) {
        UUID orgId = TenantContext.require();
        SecurityDeposit deposit = repository.findByOrgIdAndLeaseId(orgId, leaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Deposit not found for lease: " + leaseId));
        return mapper.toResponseDto(deposit);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SecurityDepositResponseDto> listDeposits(UUID leaseId, Boolean activeOnly, Pageable pageable) {
        UUID orgId = TenantContext.require();

        if (leaseId != null) {
            return repository.findByOrgIdAndLeaseId(orgId, leaseId, pageable).map(mapper::toResponseDto);
        }
        if (Boolean.TRUE.equals(activeOnly)) {
            return repository.findByOrgIdAndReturnedDateIsNull(orgId, pageable).map(mapper::toResponseDto);
        }
        return repository.findByOrgId(orgId, pageable).map(mapper::toResponseDto);
    }

    @Override
    @Transactional
    public SecurityDepositResponseDto updateDeposit(UUID id, SecurityDepositUpdateDto dto) {
        UUID orgId = TenantContext.require();
        SecurityDeposit deposit = repository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Deposit not found"));
        if (!deposit.isActive()) {
            throw new BusinessException("Cannot update a returned deposit");
        }
        if (dto.getHeldInAccountId() != null && !paymentAccountService.exists(dto.getHeldInAccountId())) {
            throw new ResourceNotFoundException("Payment account not found");
        }
        mapper.updateEntity(dto, deposit);
        SecurityDeposit updated = repository.save(deposit);
        log.info("Updated deposit {}", updated.getId());
        return mapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public void deleteDeposit(UUID id) {
        UUID orgId = TenantContext.require();
        SecurityDeposit deposit = repository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Deposit not found"));
        if (deposit.isActive()) {
            throw new BusinessException("Cannot delete an active deposit; return it first");
        }
        repository.deleteById(id);
        log.info("Deleted deposit {}", id);
    }

    @Override
    @Transactional
    public SecurityDepositResponseDto addDeduction(UUID id, DeductionRequestDto dto) {
        UUID orgId = TenantContext.require();
        SecurityDeposit deposit = repository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Deposit not found"));
        if (!deposit.isActive()) {
            throw new BusinessException("Cannot add deduction to a returned deposit");
        }
        if (dto.getAmount().compareTo(deposit.getCurrentBalance()) > 0) {
            throw new BusinessException("Deduction amount exceeds current balance");
        }
        deposit.addDeduction(dto.getAmount());
        SecurityDeposit updated = repository.save(deposit);
        log.info("Added deduction of {} to deposit {}", dto.getAmount(), id);
        return mapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public SecurityDepositResponseDto returnDeposit(UUID id, ReturnDepositRequestDto dto) {
        UUID orgId = TenantContext.require();
        SecurityDeposit deposit = repository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Deposit not found"));
        if (!deposit.isActive()) {
            throw new BusinessException("Deposit already returned");
        }
        if (dto.getReturnAmount().compareTo(deposit.getCurrentBalance()) > 0) {
            throw new BusinessException("Return amount exceeds current balance");
        }
        LocalDate returnDate = dto.getReturnedDate() != null ? dto.getReturnedDate() : LocalDate.now();
        deposit.returnDeposit(dto.getReturnAmount(), returnDate);
        SecurityDeposit updated = repository.save(deposit);
        log.info("Returned deposit {} with amount {}", id, dto.getReturnAmount());
        return mapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public void accrueInterest(UUID id, BigDecimal interestRate) {
        UUID orgId = TenantContext.require();
        SecurityDeposit deposit = repository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Deposit not found"));
        if (!deposit.isActive()) {
            throw new BusinessException("Cannot accrue interest on a returned deposit");
        }
        BigDecimal interest = deposit.getCurrentBalance().multiply(interestRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        deposit.addInterest(interest);
        repository.save(deposit);
        log.info("Accrued interest of {} on deposit {}", interest, id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(UUID depositId) {
        UUID orgId = TenantContext.get();
        if (orgId != null) {
            return repository.existsByIdAndOrgId(depositId, orgId);
        }
        return repository.existsById(depositId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isActive(UUID depositId) {
        UUID orgId = TenantContext.require();
        SecurityDeposit deposit = repository.findByIdAndOrgId(depositId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Deposit not found"));
        return deposit.isActive();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getCurrentBalance(UUID depositId) {
        UUID orgId = TenantContext.require();
        SecurityDeposit deposit = repository.findByIdAndOrgId(depositId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Deposit not found"));
        return deposit.getCurrentBalance();
    }

    @Override
    @Transactional
    public void applyDeduction(UUID depositId, BigDecimal amount) {
        UUID orgId = TenantContext.require();
        SecurityDeposit deposit = repository.findByIdAndOrgId(depositId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Deposit not found"));
        deposit.addDeduction(amount);
        repository.save(deposit);
        log.info("Applied deduction of {} to deposit {}", amount, depositId);
    }

    @Override
    @Transactional
    public void reverseDeduction(UUID depositId, BigDecimal amount) {
        UUID orgId = TenantContext.require();
        SecurityDeposit deposit = repository.findByIdAndOrgId(depositId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Deposit not found"));
        deposit.setCurrentBalance(deposit.getCurrentBalance().add(amount));
        deposit.setDeductionTotal(deposit.getDeductionTotal().subtract(amount));
        repository.save(deposit);
        log.info("Reversed deduction of {} from deposit {}", amount, depositId);
    }
}