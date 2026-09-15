package com.apex.PropertyManagementService.repository;

import com.apex.PropertyManagementService.entity.Unit;
import com.apex.PropertyManagementService.entity.enums.UnitStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UnitRepository extends JpaRepository<Unit, UUID> {
    List<Unit> findByPropertyId(UUID propertyId);
    Optional<Unit> findByUnitNumber(String unitNumber);
    List<Unit> findByStatus(UnitStatus status);
    List<Unit> findByCurrentTenantId(String tenantId);
    // ✅ REMOVED: Optional<Unit> findByUnitId(UUID unitId); – use findById() from JpaRepository instead.
}