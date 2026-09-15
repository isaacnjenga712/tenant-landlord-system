package com.apex.PaymentService.module.PaymentMethod.service.impl;

import com.apex.PaymentService.common.exception.BusinessException;
import com.apex.PaymentService.common.exception.ResourceNotFoundException;
import com.apex.PaymentService.module.PaymentMethod.dto.request.PaymentMethodCreateDto;
import com.apex.PaymentService.module.PaymentMethod.dto.request.PaymentMethodUpdateDto;
import com.apex.PaymentService.module.PaymentMethod.dto.response.PaymentMethodResponseDto;
import com.apex.PaymentService.module.PaymentMethod.entity.PaymentMethod;
import com.apex.PaymentService.module.PaymentMethod.mapper.PaymentMethodMapper;
import com.apex.PaymentService.module.PaymentMethod.repository.PaymentMethodRepository;
import com.apex.PaymentService.module.PaymentMethod.service.PaymentMethodService;
import com.apex.PaymentService.module.Tenant.service.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentMethodServiceImpl implements PaymentMethodService {

    private final PaymentMethodRepository repository;
    private final PaymentMethodMapper mapper;
    private final TenantService tenantService;

    @Override
    @Transactional
    public PaymentMethodResponseDto createPaymentMethod(PaymentMethodCreateDto dto) {
        // Validate tenant exists
        if (!tenantService.exists(dto.getTenantId())) {
            throw new ResourceNotFoundException("Tenant not found with id: " + dto.getTenantId());
        }

        // Check for duplicate gateway payment method ID (idempotency)
        if (repository.findByGatewayPaymentMethodId(dto.getGatewayPaymentMethodId()).isPresent()) {
            throw new BusinessException("Payment method already exists with this gateway ID");
        }

        PaymentMethod method = mapper.toEntity(dto);

        // If this is the first method for the tenant, or if isDefault=true, set as default
        boolean isFirst = repository.findByTenantId(dto.getTenantId(), Pageable.unpaged()).isEmpty();
        if (Boolean.TRUE.equals(dto.getIsDefault()) || isFirst) {
            // Clear existing default for tenant
            repository.clearDefaultFlagForTenant(dto.getTenantId());
            method.setIsDefault(true);
        } else {
            method.setIsDefault(false);
        }

        PaymentMethod saved = repository.save(method);
        log.info("Created payment method for tenant: {}", saved.getTenantId());
        return mapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentMethodResponseDto getPaymentMethod(UUID id) {
        PaymentMethod method = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found"));
        return mapper.toResponseDto(method);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentMethodResponseDto> getPaymentMethodsByTenant(UUID tenantId, Pageable pageable) {
        return repository.findByTenantId(tenantId, pageable).map(mapper::toResponseDto);
    }

    @Override
    @Transactional
    public PaymentMethodResponseDto updatePaymentMethod(UUID id, PaymentMethodUpdateDto dto) {
        PaymentMethod method = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found"));

        // If isDefault is being set to true, clear default for other methods of the same tenant
        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            repository.clearDefaultFlagForTenant(method.getTenantId());
            method.setIsDefault(true);
        } else if (dto.getIsDefault() != null && Boolean.FALSE.equals(dto.getIsDefault())) {
            method.setIsDefault(false);
        }

        if (dto.getIsActive() != null) {
            // If deactivating the default method, clear default flag
            if (!dto.getIsActive() && Boolean.TRUE.equals(method.getIsDefault())) {
                method.setIsDefault(false);
            }
            method.setIsActive(dto.getIsActive());
        }

        // Update other fields
        mapper.updateEntity(dto, method);

        PaymentMethod updated = repository.save(method);
        log.info("Updated payment method {}", updated.getId());
        return mapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public void deletePaymentMethod(UUID id) {
        PaymentMethod method = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found"));
        repository.deleteById(id);
        log.info("Deleted payment method {}", id);
    }

    @Override
    @Transactional
    public PaymentMethodResponseDto setDefaultMethod(UUID id) {
        PaymentMethod method = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found"));

        if (!Boolean.TRUE.equals(method.getIsActive())) {
            throw new BusinessException("Cannot set an inactive method as default");
        }

        // Clear default for all methods of this tenant, then set this one as default
        repository.clearDefaultFlagForTenant(method.getTenantId());
        method.setIsDefault(true);
        PaymentMethod updated = repository.save(method);
        log.info("Set payment method {} as default for tenant {}", id, method.getTenantId());
        return mapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public PaymentMethodResponseDto toggleActive(UUID id, boolean active) {
        PaymentMethod method = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found"));

        method.setIsActive(active);
        // If deactivating the default method, clear the default flag
        if (!active && Boolean.TRUE.equals(method.getIsDefault())) {
            method.setIsDefault(false);
        }
        PaymentMethod updated = repository.save(method);
        log.info("Toggled active status of payment method {} to {}", id, active);
        return mapper.toResponseDto(updated);
    }
}