package com.apex.PropertyManagementService.repository;

import com.apex.PropertyManagementService.entity.Unit;
import com.apex.PropertyManagementService.entity.enums.UnitStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UnitRepository extends JpaRepository<Unit, UUID> {

    // ---- org-scoped lookups ----

    Optional<Unit> findByIdAndOrgId(UUID id, UUID orgId);

    List<Unit> findByOrgId(UUID orgId);

    List<Unit> findByOrgIdAndPropertyId(UUID orgId, UUID propertyId);

    Optional<Unit> findByOrgIdAndUnitNumber(UUID orgId, String unitNumber);

    List<Unit> findByOrgIdAndStatus(UUID orgId, UnitStatus status);

    List<Unit> findByOrgIdAndCurrentTenantId(UUID orgId, String tenantId);
    
    // ---- internal use only (Kafka consumers / saga) — NOT for HTTP requests ----

    List<Unit> findByPropertyId(UUID propertyId);

    // ---- org-scoped counts ----

    long countByOrgIdAndStatus(UUID orgId, UnitStatus status);
}