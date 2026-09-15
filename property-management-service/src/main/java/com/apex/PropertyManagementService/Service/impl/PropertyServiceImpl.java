package com.apex.PropertyManagementService.Service.impl;

import com.apex.PropertyManagementService.DTOs.request.PropertyCreateRequest;

import com.apex.PropertyManagementService.DTOs.request.PropertyUpdateRequest;
import com.apex.PropertyManagementService.DTOs.response.PropertyResponse;
import com.apex.PropertyManagementService.entity.Landlord;
import com.apex.PropertyManagementService.entity.Property;
import com.apex.PropertyManagementService.entity.enums.PropertyStatus;
import com.apex.PropertyManagementService.exception.ResourceNotFoundException;
import com.apex.PropertyManagementService.mapper.PropertyMapper;
import com.apex.PropertyManagementService.producer.PropertyEventPublisher;
import com.apex.PropertyManagementService.repository.LandlordRepository;
import com.apex.PropertyManagementService.repository.PropertyRepository;
import com.apex.PropertyManagementService.Service.PropertyService;
import com.platform.common.events.property.PropertyRegisteredEvent;
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
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository propertyRepository;
    private final LandlordRepository landlordRepository;
    private final PropertyMapper mapper;
    private final PropertyEventPublisher eventPublisher;

    @Override
    @Transactional
    public PropertyResponse createProperty(PropertyCreateRequest request, String tenantHeader) {
        // ✅ Explicit conversion from String to UUID (works regardless of DTO type)
        UUID landlordUuid = UUID.fromString(request.getLandlordId().toString());
        UUID propertyUuid = UUID.fromString(request.getPropertyId().toString());

        Landlord landlord = landlordRepository.findById(landlordUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Landlord not found with id: " + request.getLandlordId()));

        propertyRepository.findByPropertyId(propertyUuid)
                .ifPresent(p -> { throw new IllegalArgumentException("Property ID already exists: " + request.getPropertyId()); });

        Property property = mapper.toEntity(request);
        property.setLandlord(landlord);
        property.setStatus(PropertyStatus.AVAILABLE);

        Property saved = propertyRepository.save(property);

        UUID tenantUuid = tenantHeader != null ? UUID.fromString(tenantHeader) : null;

        PropertyRegisteredEvent event = new PropertyRegisteredEvent(
                saved.getPropertyId(),
                landlord.getId(),
                saved.getAddressLine1(),
                saved.getAddressLine2(),
                saved.getCity(),
                saved.getState(),
                saved.getZipCode(),
                saved.getCountry(),
                saved.getStatus().name(),
                saved.getUnits() != null ? saved.getUnits().size() : 0,
                UUID.randomUUID()
        );
        eventPublisher.publishPropertyRegistered(event, tenantUuid);

        log.info("Property created: {}", saved.getId());
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropertyResponse> getAllProperties() {
        return mapper.toResponseList(propertyRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public PropertyResponse getPropertyById(UUID id) {
        Property property = findOrThrow(id);
        return mapper.toResponse(property);
    }

    @Override
    @Transactional(readOnly = true)
    public PropertyResponse getPropertyByPropertyId(String propertyId) {
        UUID propertyUuid = UUID.fromString(propertyId);
        Property property = propertyRepository.findByPropertyId(propertyUuid)
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

    @Override
    @Transactional
    public PropertyResponse updateProperty(UUID id, PropertyUpdateRequest request, String tenantHeader) {
        Property property = findOrThrow(id);
        mapper.updateEntity(property, request);
        if (request.getStatus() != null) {
            try {
                property.setStatus(PropertyStatus.valueOf(request.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status: " + request.getStatus());
            }
        }
        Property updated = propertyRepository.save(property);
        log.info("Property updated: {}", updated.getId());
        return mapper.toResponse(updated);
    }

    @Override
    @Transactional
    public PropertyResponse updatePropertyStatus(UUID id, String status, String tenantHeader) {
        Property property = findOrThrow(id);
        try {
            property.setStatus(PropertyStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
        Property updated = propertyRepository.save(property);
        log.info("Property status updated: {}", updated.getId());
        return mapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteProperty(UUID id, String tenantHeader) {
        Property property = findOrThrow(id);
        if (!property.getUnits().isEmpty()) {
            throw new IllegalStateException("Cannot delete property with existing units. Remove units first.");
        }
        propertyRepository.delete(property);
        log.info("Property deleted: {}", id);
    }

    private Property findOrThrow(UUID id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));
    }
}