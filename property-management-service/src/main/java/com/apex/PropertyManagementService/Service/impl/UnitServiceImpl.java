package com.apex.PropertyManagementService.Service.impl;

import com.apex.PropertyManagementService.DTOs.request.UnitCreateRequest;
import com.apex.PropertyManagementService.DTOs.request.UnitUpdateRequest;
import com.apex.PropertyManagementService.DTOs.response.UnitResponse;
import com.apex.PropertyManagementService.entity.Property;
import com.apex.PropertyManagementService.entity.Unit;
import com.apex.PropertyManagementService.entity.enums.UnitStatus;
import com.apex.PropertyManagementService.exception.ResourceNotFoundException;
import com.apex.PropertyManagementService.mapper.UnitMapper;
import com.apex.PropertyManagementService.repository.PropertyRepository;
import com.apex.PropertyManagementService.repository.UnitRepository;
import com.apex.PropertyManagementService.Service.UnitService;
import com.platform.common.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnitServiceImpl implements UnitService {

    private final UnitRepository unitRepository;
    private final PropertyRepository propertyRepository;
    private final UnitMapper mapper;

    @Override
    @Transactional
    public UnitResponse createUnit(UnitCreateRequest request, String tenantHeader) {
        UUID orgId = TenantContext.require();

        Property property = propertyRepository.findByIdAndOrgId(request.getPropertyId(), orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found: " + request.getPropertyId()));

        String requestedNumber = request.getUnitNumber();
        final String unitNumber;
        if (requestedNumber == null || requestedNumber.isBlank()) {
            long existing = unitRepository.findByOrgIdAndPropertyId(orgId, property.getId()).size();
            unitNumber = String.valueOf((char) ('A' + existing));
        } else {
            unitNumber = requestedNumber;
        }

        unitRepository.findByOrgIdAndUnitNumber(orgId, unitNumber)
                .ifPresent(u -> { throw new IllegalArgumentException("Unit number already exists: " + unitNumber); });

        Unit unit = mapper.toEntity(request);
        unit.setOrgId(orgId);
        unit.setUnitNumber(unitNumber);
        unit.setProperty(property);
        unit.setStatus(UnitStatus.AVAILABLE);

        Unit saved = unitRepository.save(unit);
        log.info("Unit created: {} number={} org={}", saved.getId(), unitNumber, orgId);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitResponse> getAllUnits() {
        UUID orgId = TenantContext.require();
        return mapper.toResponseList(unitRepository.findByOrgId(orgId));
    }

    @Override
    @Transactional(readOnly = true)
    public UnitResponse getUnitById(UUID id) {
        Unit unit = findOrThrow(id);
        return mapper.toResponse(unit);
    }

    @Override
    @Transactional(readOnly = true)
    public UnitResponse getUnitByUnitNumber(String unitNumber) {
        UUID orgId = TenantContext.require();
        Unit unit = unitRepository.findByOrgIdAndUnitNumber(orgId, unitNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found: " + unitNumber));
        return mapper.toResponse(unit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitResponse> getUnitsByProperty(UUID propertyId) {
        UUID orgId = TenantContext.require();
        propertyRepository.findByIdAndOrgId(propertyId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found: " + propertyId));
        return mapper.toResponseList(unitRepository.findByOrgIdAndPropertyId(orgId, propertyId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitResponse> getAvailableUnitsByProperty(UUID propertyId) {
        UUID orgId = TenantContext.require();
        propertyRepository.findByIdAndOrgId(propertyId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found: " + propertyId));
        List<Unit> units = unitRepository.findByOrgIdAndPropertyId(orgId, propertyId)
                .stream().filter(u -> u.getStatus() == UnitStatus.AVAILABLE).collect(Collectors.toList());
        return mapper.toResponseList(units);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitResponse> getUnitsByStatus(String status) {
        UUID orgId = TenantContext.require();
        try {
            UnitStatus enumStatus = UnitStatus.valueOf(status.toUpperCase());
            return unitRepository.findByOrgIdAndStatus(orgId, enumStatus)
                    .stream().map(mapper::toResponse).collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitResponse> getUnitsByTenant(UUID tenantId) {
        UUID orgId = TenantContext.require();
        return unitRepository.findByOrgIdAndCurrentTenantId(orgId, tenantId.toString())
                .stream().map(mapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UnitResponse updateUnit(UUID id, UnitUpdateRequest request, String tenantHeader) {
        Unit unit = findOrThrow(id);
        mapper.updateEntity(unit, request);
        if (request.getStatus() != null) {
            try {
                unit.setStatus(UnitStatus.valueOf(request.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status: " + request.getStatus());
            }
        }
        Unit updated = unitRepository.save(unit);
        log.info("Unit updated: {}", updated.getId());
        return mapper.toResponse(updated);
    }

    @Override
    @Transactional
    public UnitResponse updateUnitStatus(UUID id, String status, String tenantHeader) {
        Unit unit = findOrThrow(id);
        try {
            unit.setStatus(UnitStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
        Unit updated = unitRepository.save(unit);
        log.info("Unit status updated: {}", updated.getId());
        return mapper.toResponse(updated);
    }

    @Override
    @Transactional
    public UnitResponse assignTenant(UUID id, UUID tenantId, String tenantHeader) {
        Unit unit = findOrThrow(id);
        if (unit.getStatus() == UnitStatus.OCCUPIED) {
            throw new IllegalStateException("Unit already occupied");
        }
        unit.setStatus(UnitStatus.OCCUPIED);
        unit.setCurrentTenantId(tenantId.toString());
        Unit updated = unitRepository.save(unit);
        log.info("Tenant {} assigned to unit {}", tenantId, updated.getId());
        return mapper.toResponse(updated);
    }

    @Override
    @Transactional
    public UnitResponse vacateUnit(UUID id, String tenantHeader) {
        Unit unit = findOrThrow(id);
        if (unit.getStatus() != UnitStatus.OCCUPIED) {
            throw new IllegalStateException("Unit is not occupied");
        }
        unit.setStatus(UnitStatus.AVAILABLE);
        unit.setCurrentTenantId(null);
        Unit updated = unitRepository.save(unit);
        log.info("Unit {} vacated", updated.getId());
        return mapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteUnit(UUID id, String tenantHeader) {
        Unit unit = findOrThrow(id);
        if (unit.getStatus() == UnitStatus.OCCUPIED) {
            throw new IllegalStateException("Cannot delete occupied unit. Vacate first.");
        }
        unitRepository.delete(unit);
        log.info("Unit deleted: {}", id);
    }

    private Unit findOrThrow(UUID id) {
        UUID orgId = TenantContext.require();
        return unitRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found with id: " + id));
    }
}