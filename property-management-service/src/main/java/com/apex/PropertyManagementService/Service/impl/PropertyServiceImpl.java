package com.apex.PropertyManagementService.Service.impl;

import com.apex.PropertyManagementService.DTOs.request.PropertyCreateRequest;
import com.apex.PropertyManagementService.DTOs.request.PropertyUpdateRequest;
import com.apex.PropertyManagementService.DTOs.response.PropertyResponse;
import com.apex.PropertyManagementService.entity.Landlord;
import com.apex.PropertyManagementService.entity.Property;
import com.apex.PropertyManagementService.entity.enums.LandlordStatus;
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
    public PropertyResponse createProperty(PropertyCreateRequest request, String tenantHeader, String userHeader) {
        UUID propertyUuid = request.getPropertyId() != null
                ? request.getPropertyId()
                : UUID.randomUUID();

        // Resolve landlord — prefer X-User-Id (gateway injects publicId), tolerate legacy values
        Landlord landlord = resolveLandlord(userHeader, request.getLandlordId());

        propertyRepository.findByPropertyId(propertyUuid)
                .ifPresent(p -> { throw new IllegalArgumentException("Property ID already exists: " + propertyUuid); });

        Property property = mapper.toEntity(request);
        property.setPropertyId(propertyUuid);
        property.setLandlord(landlord);
        property.setStatus(PropertyStatus.AVAILABLE);

        Property saved = propertyRepository.save(property);

        // Tenant header may be "default" or any non-UUID string — parse defensively
        UUID tenantUuid = null;
        if (tenantHeader != null && !tenantHeader.isBlank()) {
            try {
                tenantUuid = UUID.fromString(tenantHeader);
            } catch (IllegalArgumentException e) {
                log.warn("Ignoring non-UUID X-Tenant-ID header: {}", tenantHeader);
            }
        }

        PropertyRegisteredEvent event = new PropertyRegisteredEvent(
                saved.getPropertyId(),
                landlord.getUserId() != null ? landlord.getUserId() : landlord.getId(),
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

        log.info("Property created: {} landlordId={}", saved.getId(),
                landlord.getUserId() != null ? landlord.getUserId() : landlord.getId());
        return mapper.toResponse(saved);
    }

    /**
     * Resolve the landlord for a new property.
     * Priority:
     *   1. X-User-Id header (the authenticated user's publicId UUID) — find or create landlord
     *   2. X-User-Id header as email — find or create by email
     *   3. request.landlordId (legacy fallback) — look up by internal Landlord.id
     */
    private Landlord resolveLandlord(String userHeader, UUID fallbackLandlordId) {
        if (userHeader != null && !userHeader.isBlank()) {
            String raw = userHeader.trim();

            // Try UUID first
            UUID userPublicId = null;
            try {
                userPublicId = UUID.fromString(raw);
            } catch (IllegalArgumentException ignored) {
                log.warn("X-User-Id is not a UUID: '{}' — falling back to email lookup", raw);
            }

            if (userPublicId != null) {
                final UUID lookupId = userPublicId;
                return landlordRepository.findByUserId(lookupId)
                        .orElseGet(() -> {
                            Landlord created = new Landlord();
                            created.setUserId(lookupId);
                            created.setEmail("landlord-" + lookupId.toString().substring(0, 8) + "@rentflow.local");
                            created.setFirstName("Landlord");
                            created.setLastName(lookupId.toString().substring(0, 8));
                            created.setCompanyName("RentFlow");
                            created.setStatus(LandlordStatus.ACTIVE);
                            log.info("Auto-created Landlord for userId={}", lookupId);
                            return landlordRepository.save(created);
                        });
            }

            // Non-UUID → treat as email
            String email = raw;
            return landlordRepository.findByEmail(email)
                    .orElseGet(() -> {
                        Landlord created = new Landlord();
                        created.setUserId(UUID.randomUUID());
                        created.setEmail(email);
                        created.setFirstName("Landlord");
                        created.setLastName(email.contains("@") ? email.split("@")[0] : email);
                        created.setCompanyName("RentFlow");
                        created.setStatus(LandlordStatus.ACTIVE);
                        log.info("Auto-created Landlord from email fallback: {}", email);
                        return landlordRepository.save(created);
                    });
        }

        if (fallbackLandlordId != null) {
            return landlordRepository.findByUserId(fallbackLandlordId)
                    .or(() -> landlordRepository.findById(fallbackLandlordId))
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Landlord not found: " + fallbackLandlordId));
        }

        throw new IllegalArgumentException("No X-User-Id and no landlordId to resolve landlord");
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
        Landlord landlord = landlordRepository.findByUserId(landlordId)
                .orElseGet(() -> landlordRepository.findById(landlordId).orElse(null));

        if (landlord == null) {
            return List.of();
        }
        List<Property> properties = propertyRepository.findByLandlordId(landlord.getId());
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