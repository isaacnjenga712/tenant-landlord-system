package com.apex.PaymentService.module.PaymentAccount.service.impl;

import com.apex.PaymentService.common.exception.BusinessException;
import com.apex.PaymentService.common.exception.ResourceNotFoundException;
import com.apex.PaymentService.module.PaymentAccount.dto.request.BalanceAdjustmentDto;
import com.apex.PaymentService.module.PaymentAccount.dto.request.PaymentAccountCreateDto;
import com.apex.PaymentService.module.PaymentAccount.dto.request.PaymentAccountUpdateDto;
import com.apex.PaymentService.module.PaymentAccount.dto.request.TransferRequestDto;
import com.apex.PaymentService.module.PaymentAccount.dto.response.PaymentAccountResponseDto;
import com.apex.PaymentService.module.PaymentAccount.entity.PaymentAccount;
import com.apex.PaymentService.module.PaymentAccount.enums.EntityType;
import com.apex.PaymentService.module.PaymentAccount.mapper.PaymentAccountMapper;
import com.apex.PaymentService.module.PaymentAccount.repository.PaymentAccountRepository;
import com.apex.PaymentService.module.PaymentAccount.service.PaymentAccountService;
import com.apex.PaymentService.module.PaymentAccount.service.external.EntityValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentAccountServiceImpl implements PaymentAccountService {

    private final PaymentAccountRepository repository;
    private final PaymentAccountMapper mapper;
    private final EntityValidationService entityValidationService;

    @Override
    @Transactional
    public PaymentAccountResponseDto createAccount(PaymentAccountCreateDto dto) {
        if (!entityValidationService.exists(dto.getEntityType(), dto.getEntityId())) {
            throw new ResourceNotFoundException(
                String.format("%s with ID %s not found", dto.getEntityType(), dto.getEntityId())
            );
        }
        if (repository.findByEntityTypeAndEntityId(dto.getEntityType(), dto.getEntityId()).isPresent()) {
            throw new BusinessException("Account already exists for this entity");
        }

        PaymentAccount account = mapper.toEntity(dto);
        PaymentAccount saved = repository.save(account);
        log.info("Created payment account for {} with id {}", dto.getEntityType(), saved.getId());
        return mapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentAccountResponseDto getAccount(UUID id) {
        PaymentAccount account = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        return mapper.toResponseDto(account);
    }

    // ✅ ADDED MISSING METHOD
    @Override
    @Transactional(readOnly = true)
    public PaymentAccountResponseDto getAccountByEntity(EntityType entityType, UUID entityId) {
        PaymentAccount account = repository.findByEntityTypeAndEntityId(entityType, entityId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found for entity"));
        return mapper.toResponseDto(account);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentAccountResponseDto> listAccounts(EntityType entityType, Pageable pageable) {
        if (entityType != null) {
            return repository.findByEntityType(entityType, pageable).map(mapper::toResponseDto);
        }
        return repository.findAll(pageable).map(mapper::toResponseDto);
    }

    @Override
    @Transactional
    public PaymentAccountResponseDto updateAccount(UUID id, PaymentAccountUpdateDto dto) {
        PaymentAccount account = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (dto.getCurrency() != null) {
            account.setCurrency(dto.getCurrency().toUpperCase());
        }
        if (dto.getIsActive() != null) {
            account.setIsActive(dto.getIsActive());
        }

        PaymentAccount updated = repository.save(account);
        log.info("Updated account {}", updated.getId());
        return mapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public void deleteAccount(UUID id) {
        PaymentAccount account = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (account.getTrustBalance().compareTo(BigDecimal.ZERO) != 0 ||
            account.getOperationalBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessException("Cannot delete account with non-zero balance");
        }
        account.setIsActive(false);
        repository.save(account);
        log.info("Deleted account {}", id);
    }

    @Override
    @Transactional
    public PaymentAccountResponseDto adjustBalance(UUID id, BalanceAdjustmentDto dto) {
        PaymentAccount account = repository.findByIdWithLock(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        BigDecimal delta = dto.getAmount();
        boolean isTrust = "trust".equalsIgnoreCase(dto.getTargetBalance());

        if (isTrust) {
            account.addToTrust(delta);
        } else {
            account.addToOperational(delta);
        }

        PaymentAccount updated = repository.save(account);
        log.info("Adjusted {} balance of account {} by {}", dto.getTargetBalance(), id, delta);
        return mapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public PaymentAccountResponseDto transferBalance(UUID id, TransferRequestDto dto) {
        if (dto.getFromBalance().equalsIgnoreCase(dto.getToBalance())) {
            throw new BusinessException("Source and destination balances must be different");
        }

        PaymentAccount account = repository.findByIdWithLock(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        BigDecimal amount = dto.getAmount();
        BigDecimal trustDelta = BigDecimal.ZERO;
        BigDecimal opDelta = BigDecimal.ZERO;

        if ("trust".equalsIgnoreCase(dto.getFromBalance()) && "operational".equalsIgnoreCase(dto.getToBalance())) {
            if (account.getTrustBalance().compareTo(amount) < 0) {
                throw new BusinessException("Insufficient trust balance for transfer");
            }
            trustDelta = amount.negate();
            opDelta = amount;
        } else if ("operational".equalsIgnoreCase(dto.getFromBalance()) && "trust".equalsIgnoreCase(dto.getToBalance())) {
            if (account.getOperationalBalance().compareTo(amount) < 0) {
                throw new BusinessException("Insufficient operational balance for transfer");
            }
            trustDelta = amount;
            opDelta = amount.negate();
        } else {
            throw new BusinessException("Invalid balance type specified. Use 'trust' or 'operational'.");
        }

        account.addToTrust(trustDelta);
        account.addToOperational(opDelta);
        PaymentAccount updated = repository.save(account);
        log.info("Transferred {} from {} to {} on account {}", amount, dto.getFromBalance(), dto.getToBalance(), id);
        return mapper.toResponseDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(UUID id) {
        return repository.existsById(id);
    }
}
