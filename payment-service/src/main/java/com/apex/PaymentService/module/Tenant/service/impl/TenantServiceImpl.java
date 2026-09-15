package com.apex.PaymentService.module.Tenant.service.impl;

import com.apex.PaymentService.common.exception.BusinessException;
import com.apex.PaymentService.common.exception.ResourceNotFoundException;
import com.apex.PaymentService.module.Tenant.dto.request.TenantCreateDto;
import com.apex.PaymentService.module.Tenant.dto.request.TenantUpdateDto;
import com.apex.PaymentService.module.Tenant.dto.response.TenantResponseDto;
import com.apex.PaymentService.module.Tenant.entity.Tenant;
import com.apex.PaymentService.module.Tenant.enums.TenantStatus;
import com.apex.PaymentService.module.Tenant.mapper.TenantMapper;
import com.apex.PaymentService.module.Tenant.repository.TenantRepository;
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
public class TenantServiceImpl implements TenantService {

    private final TenantRepository repository;
    private final TenantMapper mapper;

    @Override
    @Transactional
    public TenantResponseDto createTenant(TenantCreateDto dto) {
        if (repository.findByEmail(dto.getEmail()).isPresent()) {
            throw new BusinessException("Email already exists: " + dto.getEmail());
        }
        Tenant tenant = mapper.toEntity(dto);
        if (tenant.getStatus() == null) tenant.setStatus(TenantStatus.active);
        Tenant saved = repository.save(tenant);
        log.info("Created tenant: {}", saved.getEmail());
        return mapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TenantResponseDto getTenant(UUID id) {
        Tenant tenant = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));
        return mapper.toResponseDto(tenant);
    }

    @Override
    @Transactional(readOnly = true)
    public TenantResponseDto getTenantByEmail(String email) {
        Tenant tenant = repository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with email: " + email));
        return mapper.toResponseDto(tenant);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TenantResponseDto> listTenants(TenantStatus status, Pageable pageable) {
        if (status != null) {
            return repository.findByStatus(status, pageable).map(mapper::toResponseDto);
        }
        return repository.findAll(pageable).map(mapper::toResponseDto);
    }

    @Override
    @Transactional
    public TenantResponseDto updateTenant(UUID id, TenantUpdateDto dto) {
        Tenant tenant = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));
        if (dto.getEmail() != null && !dto.getEmail().equals(tenant.getEmail())) {
            if (repository.findByEmail(dto.getEmail()).isPresent()) {
                throw new BusinessException("Email already in use: " + dto.getEmail());
            }
        }
        mapper.updateEntity(dto, tenant);
        Tenant updated = repository.save(tenant);
        log.info("Updated tenant: {}", updated.getId());
        return mapper.toResponseDto(updated);
    }
    
    @Override
    @Transactional
    public TenantResponseDto updateFull(UUID id, TenantCreateDto dto) {
        // 1. Fetch existing tenant
        Tenant tenant = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        // 2. Check email uniqueness (if changed)
        if (!dto.getEmail().equals(tenant.getEmail()) &&
            repository.findByEmail(dto.getEmail()).isPresent()) {
            throw new BusinessException("Email already in use: " + dto.getEmail());
        }

        // 3. Map all fields from dto to entity (full replacement)
        // Since TenantCreateDto has all required fields, map them all
        tenant.setFirstName(dto.getFirstName());
        tenant.setLastName(dto.getLastName());
        tenant.setEmail(dto.getEmail());
        tenant.setPhone(dto.getPhone());
        tenant.setDateOfBirth(dto.getDateOfBirth());
        tenant.setStatus(dto.getStatus() != null ? dto.getStatus() : TenantStatus.active);

        // 4. Save and return
        Tenant updated = repository.save(tenant);
        log.info("Full updated tenant: {}", updated.getId());
        return mapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public void deleteTenant(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Tenant not found");
        }
        repository.deleteById(id);
        log.info("Deleted tenant: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(UUID id) {
        return repository.existsById(id);
    }
}
