package com.apex.PropertyManagementService.repository;

import com.apex.PropertyManagementService.entity.Property;
import com.apex.PropertyManagementService.entity.enums.PropertyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PropertyRepository extends JpaRepository<Property, UUID> {
    Optional<Property> findByPropertyId(UUID propertyId);
    List<Property> findByLandlordId(UUID landlordId);
    List<Property> findByStatus(PropertyStatus status);
    List<Property> findByCity(String city);
    // ✅ REMOVED: Optional<Unit> findByUnitId(UUID unitId); – it belongs in UnitRepository
}