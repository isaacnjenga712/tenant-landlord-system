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
import com.platform.common.tenant.TenantContext;
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
        UUID orgId = TenantContext.require();

        if (!tenantService.exists(dto.getTenantId())) {
            throw new ResourceNotFoundException("Tenant not found with id: " + dto.getTenantId());
        }

        if (repository.findByOrgIdAndGatewayPaymentMethodId(orgId, dto.getGatewayPaymentMethodId()).isPresent()) {
            throw new BusinessException("Payment method already exists with this gateway ID");
        }

        PaymentMethod method = mapper.toEntity(dto);
        method.setOrgId(orgId);

        boolean isFirst = repository.findByOrgIdAndTenantId(orgId, dto.getTenantId(), Pageable.unpaged()).isEmpty();
        if (Boolean.TRUE.equals(dto.getIsDefault()) || isFirst) {
            repository.clearDefaultFlagForOrgAndTenant(orgId, dto.getTenantId());
            method.setIsDefault(true);
        } else {
            method.setIsDefault(false);
        }

        PaymentMethod saved = repository.save(method);
        log.info("Created payment method for tenant: {} org {}", saved.getTenantId(), orgId);
        return mapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentMethodResponseDto getPaymentMethod(UUID id) {
        UUID orgId = TenantContext.require();
        PaymentMethod method = repository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found"));
        return mapper.toResponseDto(method);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentMethodResponseDto> getPaymentMethodsByTenant(UUID tenantId, Pageable pageable) {
        UUID orgId = TenantContext.require();
        return repository.findByOrgIdAndTenantId(orgId, tenantId, pageable).map(mapper::toResponseDto);
    }

    @Override
    @Transactional
    public PaymentMethodResponseDto updatePaymentMethod(UUID id, PaymentMethodUpdateDto dto) {
        UUID orgId = TenantContext.require();
        PaymentMethod method = repository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found"));

        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            repository.clearDefaultFlagForOrgAndTenant(orgId, method.getTenantId());
            method.setIsDefault(true);
        } else if (dto.getIsDefault() != null && Boolean.FALSE.equals(dto.getIsDefault())) {
            method.setIsDefault(false);
        }

        if (dto.getIsActive() != null) {
            if (!dto.getIsActive() && Boolean.TRUE.equals(method.getIsDefault())) {
                method.setIsDefault(false);
            }
            method.setIsActive(dto.getIsActive());
        }

        mapper.updateEntity(dto, method);

        PaymentMethod updated = repository.save(method);
        log.info("Updated payment method {}", updated.getId());
        return mapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public void deletePaymentMethod(UUID id) {
        UUID orgId = TenantContext.require();
        PaymentMethod method = repository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found"));
        repository.deleteById(id);
        log.info("Deleted payment method {}", id);
    }

    @Override
    @Transactional
    public PaymentMethodResponseDto setDefaultMethod(UUID id) {
        UUID orgId = TenantContext.require();
        PaymentMethod method = repository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found"));

        if (!Boolean.TRUE.equals(method.getIsActive())) {
            throw new BusinessException("Cannot set an inactive method as default");
        }

        repository.clearDefaultFlagForOrgAndTenant(orgId, method.getTenantId());
        method.setIsDefault(true);
        PaymentMethod updated = repository.save(method);
        log.info("Set payment method {} as default for tenant {} org {}", id, method.getTenantId(), orgId);
        return mapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public PaymentMethodResponseDto toggleActive(UUID id, boolean active) {
        UUID orgId = TenantContext.require();
        PaymentMethod method = repository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found"));

        method.setIsActive(active);
        if (!active && Boolean.TRUE.equals(method.getIsDefault())) {
            method.setIsDefault(false);
        }
        PaymentMethod updated = repository.save(method);
        log.info("Toggled active status of payment method {} to {}", id, active);
        return mapper.toResponseDto(updated);
    }
}