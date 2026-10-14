package com.apex.PropertyManagementService.repository;

import com.apex.PropertyManagementService.entity.Property;
import com.apex.PropertyManagementService.entity.enums.PropertyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PropertyRepository extends JpaRepository<Property, UUID> {

    // ---- org-scoped lookups ----

    Optional<Property> findByIdAndOrgId(UUID id, UUID orgId);

    Optional<Property> findByPropertyIdAndOrgId(UUID propertyId, UUID orgId);

    List<Property> findByOrgId(UUID orgId);

    List<Property> findByOrgIdAndLandlordId(UUID orgId, UUID landlordId);

    List<Property> findByOrgIdAndStatus(UUID orgId, PropertyStatus status);

    List<Property> findByOrgIdAndCity(UUID orgId, String city);
    
    Optional<Property> findByPropertyId(UUID propertyId);

    // ---- org-scoped counts ----

    long countByOrgIdAndStatus(UUID orgId, PropertyStatus status);
}