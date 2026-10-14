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
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository propertyRepository;
    private final LandlordRepository landlordRepository;
    private final PropertyMapper mapper;
    private final PropertyEventPublisher eventPublisher;

    @Override
    @Transactional
    public PropertyResponse createProperty(PropertyCreateRequest request, String tenantHeader, String userHeader) {
        UUID orgId = TenantContext.require();

        UUID propertyUuid = request.getPropertyId() != null
                ? request.getPropertyId()
                : UUID.randomUUID();

        Landlord landlord = resolveLandlord(userHeader, request.getLandlordId(), orgId);

        propertyRepository.findByPropertyIdAndOrgId(propertyUuid, orgId)
                .ifPresent(p -> { throw new IllegalArgumentException("Property ID already exists: " + propertyUuid); });

        Property property = mapper.toEntity(request);
        property.setPropertyId(propertyUuid);
        property.setLandlord(landlord);
        property.setStatus(PropertyStatus.AVAILABLE);
        property.setOrgId(orgId);

        Property saved = propertyRepository.save(property);

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
                orgId
        );
        eventPublisher.publishPropertyRegistered(event, orgId);

        log.info("Property created: {} orgId={} landlordId={}", saved.getId(), orgId,
                landlord.getUserId() != null ? landlord.getUserId() : landlord.getId());
        return mapper.toResponse(saved);
    }

    private Landlord resolveLandlord(String userHeader, UUID fallbackLandlordId, UUID orgId) {
        if (userHeader != null && !userHeader.isBlank()) {
            String raw = userHeader.trim();

            UUID userPublicId = null;
            try {
                userPublicId = UUID.fromString(raw);
            } catch (IllegalArgumentException ignored) {
                log.warn("X-User-Id is not a UUID: '{}' — falling back to email lookup", raw);
            }

            if (userPublicId != null) {
                final UUID lookupId = userPublicId;
                return landlordRepository.findByUserIdAndOrgId(lookupId, orgId)
                        .orElseGet(() -> {
                            Landlord created = new Landlord();
                            created.setOrgId(orgId);
                            created.setUserId(lookupId);
                            created.setEmail("landlord-" + lookupId.toString().substring(0, 8) + "@rentflow.local");
                            created.setFirstName("Landlord");
                            created.setLastName(lookupId.toString().substring(0, 8));
                            created.setCompanyName("RentFlow");
                            created.setStatus(LandlordStatus.ACTIVE);
                            log.info("Auto-created Landlord for userId={} org={}", lookupId, orgId);
                            return landlordRepository.save(created);
                        });
            }

            String email = raw;
            return landlordRepository.findByEmailAndOrgId(email, orgId)
                    .orElseGet(() -> {
                        Landlord created = new Landlord();
                        created.setOrgId(orgId);
                        created.setUserId(UUID.randomUUID());
                        created.setEmail(email);
                        created.setFirstName("Landlord");
                        created.setLastName(email.contains("@") ? email.split("@")[0] : email);
                        created.setCompanyName("RentFlow");
                        created.setStatus(LandlordStatus.ACTIVE);
                        log.info("Auto-created Landlord from email fallback: {} org={}", email, orgId);
                        return landlordRepository.save(created);
                    });
        }

        if (fallbackLandlordId != null) {
            return landlordRepository.findByIdAndOrgId(fallbackLandlordId, orgId)
                    .or(() -> landlordRepository.findByUserIdAndOrgId(fallbackLandlordId, orgId))
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Landlord not found: " + fallbackLandlordId));
        }

        throw new IllegalArgumentException("No X-User-Id and no landlordId to resolve landlord");
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropertyResponse> getAllProperties() {
        UUID orgId = TenantContext.require();
        return mapper.toResponseList(propertyRepository.findByOrgId(orgId));
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
        UUID orgId = TenantContext.require();
        UUID propertyUuid = UUID.fromString(propertyId);
        Property property = propertyRepository.findByPropertyIdAndOrgId(propertyUuid, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with ID: " + propertyId));
        return mapper.toResponse(property);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropertyResponse> getPropertiesByLandlord(UUID landlordId) {
        UUID orgId = TenantContext.require();
        Landlord landlord = landlordRepository.findByUserIdAndOrgId(landlordId, orgId)
                .or(() -> landlordRepository.findByIdAndOrgId(landlordId, orgId))
                .orElse(null);

        if (landlord == null) {
            return List.of();
        }
        return mapper.toResponseList(propertyRepository.findByOrgIdAndLandlordId(orgId, landlord.getId()));
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
        UUID orgId = TenantContext.require();
        return propertyRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));
    }
}