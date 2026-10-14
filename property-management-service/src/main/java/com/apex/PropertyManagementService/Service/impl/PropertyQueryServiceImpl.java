package com.apex.PropertyManagementService.Service.impl;

import com.apex.PropertyManagementService.DTOs.response.PropertyResponse;
import com.apex.PropertyManagementService.entity.Property;
import com.apex.PropertyManagementService.entity.enums.PropertyStatus;
import com.apex.PropertyManagementService.exception.ResourceNotFoundException;
import com.apex.PropertyManagementService.mapper.PropertyMapper;
import com.apex.PropertyManagementService.repository.PropertyRepository;
import com.apex.PropertyManagementService.Service.PropertyQueryService;
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
public class PropertyQueryServiceImpl implements PropertyQueryService {

    private final PropertyRepository propertyRepository;
    private final PropertyMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<PropertyResponse> getAllProperties() {
        UUID orgId = TenantContext.require();
        return mapper.toResponseList(propertyRepository.findByOrgId(orgId));
    }

    @Override
    @Transactional(readOnly = true)
    public PropertyResponse getPropertyById(UUID id) {
        UUID orgId = TenantContext.require();
        Property property = propertyRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));
        return mapper.toResponse(property);
    }

    @Override
    @Transactional(readOnly = true)
    public PropertyResponse getPropertyByPropertyId(UUID propertyId) {
        UUID orgId = TenantContext.require();
        Property property = propertyRepository.findByPropertyIdAndOrgId(propertyId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with ID: " + propertyId));
        return mapper.toResponse(property);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropertyResponse> getPropertiesByLandlord(UUID landlordId) {
        UUID orgId = TenantContext.require();
        return mapper.toResponseList(propertyRepository.findByOrgIdAndLandlordId(orgId, landlordId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropertyResponse> getPropertiesByStatus(String status) {
        UUID orgId = TenantContext.require();
        try {
            PropertyStatus enumStatus = PropertyStatus.valueOf(status.toUpperCase());
            return propertyRepository.findByOrgIdAndStatus(orgId, enumStatus)
                    .stream().map(mapper::toResponse).collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropertyResponse> getPropertiesByCity(String city) {
        UUID orgId = TenantContext.require();
        return propertyRepository.findByOrgIdAndCity(orgId, city)
                .stream().map(mapper::toResponse).collect(Collectors.toList());
    }
}