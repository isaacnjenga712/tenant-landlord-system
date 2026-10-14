package com.apex.PropertyManagementService.repository;

import com.apex.PropertyManagementService.entity.Landlord;
import com.apex.PropertyManagementService.entity.enums.LandlordStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LandlordRepository extends JpaRepository<Landlord, UUID> {

    // ---- org-scoped lookups ----

    Optional<Landlord> findByIdAndOrgId(UUID id, UUID orgId);

    Optional<Landlord> findByEmailAndOrgId(String email, UUID orgId);

    Optional<Landlord> findByUserIdAndOrgId(UUID userId, UUID orgId);

    List<Landlord> findByOrgId(UUID orgId);

    List<Landlord> findByOrgIdAndStatus(UUID orgId, LandlordStatus status);

    boolean existsByEmailAndOrgId(String email, UUID orgId);

    // ---- org-scoped counts ----

    long countByOrgIdAndStatus(UUID orgId, LandlordStatus status);
}