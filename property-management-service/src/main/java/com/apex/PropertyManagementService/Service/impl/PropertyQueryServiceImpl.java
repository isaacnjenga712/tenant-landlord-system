package com.apex.PropertyManagementService.Service.impl;

import com.apex.PropertyManagementService.DTOs.response.PropertyResponse;
import com.apex.PropertyManagementService.entity.Property;
import com.apex.PropertyManagementService.entity.enums.PropertyStatus;
import com.apex.PropertyManagementService.exception.ResourceNotFoundException;
import com.apex.PropertyManagementService.mapper.PropertyMapper;
import com.apex.PropertyManagementService.repository.PropertyRepository;
import com.apex.PropertyManagementService.Service.PropertyQueryService;
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
        return mapper.toResponseList(propertyRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public PropertyResponse getPropertyById(UUID id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));
        return mapper.toResponse(property);
    }

    // ✅ Changed parameter from String to UUID
    @Override
    @Transactional(readOnly = true)
    public PropertyResponse getPropertyByPropertyId(UUID propertyId) {
        Property property = propertyRepository.findByPropertyId(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with ID: " + propertyId));
        return mapper.toResponse(property);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropertyResponse> getPropertiesByLandlord(UUID landlordId) {
        List<Property> properties = propertyRepository.findByLandlordId(landlordId);
        return mapper.toResponseList(properties);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropertyResponse> getPropertiesByStatus(String status) {
        try {
            PropertyStatus enumStatus = PropertyStatus.valueOf(status.toUpperCase());
            return propertyRepository.findByStatus(enumStatus)
                    .stream().map(mapper::toResponse).collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropertyResponse> getPropertiesByCity(String city) {
        return propertyRepository.findByCity(city)
                .stream().map(mapper::toResponse).collect(Collectors.toList());
    }
}
