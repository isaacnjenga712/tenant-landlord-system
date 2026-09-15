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
        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property not found: " + request.getPropertyId()));

        unitRepository.findByUnitNumber(request.getUnitNumber())
                .ifPresent(u -> { throw new IllegalArgumentException("Unit number already exists: " + request.getUnitNumber()); });

        Unit unit = mapper.toEntity(request);
        unit.setProperty(property);
        unit.setStatus(UnitStatus.AVAILABLE);

        Unit saved = unitRepository.save(unit);
        log.info("Unit created: {}", saved.getId());
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitResponse> getAllUnits() {
        return mapper.toResponseList(unitRepository.findAll());
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
        Unit unit = unitRepository.findByUnitNumber(unitNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found: " + unitNumber));
        return mapper.toResponse(unit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitResponse> getUnitsByProperty(UUID propertyId) {
        propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found: " + propertyId));
        List<Unit> units = unitRepository.findByPropertyId(propertyId);
        return mapper.toResponseList(units);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitResponse> getAvailableUnitsByProperty(UUID propertyId) {
        propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found: " + propertyId));
        List<Unit> units = unitRepository.findByPropertyId(propertyId)
                .stream().filter(u -> u.getStatus() == UnitStatus.AVAILABLE).collect(Collectors.toList());
        return mapper.toResponseList(units);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitResponse> getUnitsByStatus(String status) {
        try {
            UnitStatus enumStatus = UnitStatus.valueOf(status.toUpperCase());
            return unitRepository.findByStatus(enumStatus)
                    .stream().map(mapper::toResponse).collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitResponse> getUnitsByTenant(UUID tenantId) {
        return unitRepository.findByCurrentTenantId(tenantId.toString())
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
        // ✅ Convert UUID to String
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
        return unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found with id: " + id));
    }
}