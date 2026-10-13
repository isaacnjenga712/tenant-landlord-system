package com.apex.module.lease.repository;

import com.apex.module.lease.entity.Lease;
import com.apex.module.lease.enums.LeaseStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeaseRepository extends JpaRepository<Lease, UUID>,
                                        JpaSpecificationExecutor<Lease> {

    // ---- org-scoped lookups ----

    Optional<Lease> findByIdAndOrgId(UUID id, UUID orgId);

    List<Lease> findByOrgIdAndTenantId(UUID orgId, UUID tenantId);

    List<Lease> findByOrgIdAndLandlordId(UUID orgId, UUID landlordId);

    List<Lease> findByOrgIdAndPropertyId(UUID orgId, UUID propertyId);

    List<Lease> findByOrgIdAndStatus(UUID orgId, LeaseStatus status);

    @Query("SELECT l FROM Lease l WHERE l.orgId = :orgId " +
           "AND l.propertyId = :propertyId " +
           "AND l.startDate <= :endDate AND l.endDate >= :startDate")
    List<Lease> findOverlappingLeases(@Param("orgId") UUID orgId,
                                      @Param("propertyId") UUID propertyId,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate);

    // ---- org-scoped KPI counts ----

    long countByOrgIdAndStatus(UUID orgId, LeaseStatus status);

    long countByOrgIdAndCreatedAtAfter(UUID orgId, LocalDateTime after);

    long countByOrgIdAndStatusAndUpdatedAtAfter(UUID orgId, LeaseStatus status, LocalDateTime after);

    long countByOrgIdAndCreatedAtBetween(UUID orgId, LocalDateTime from, LocalDateTime to);
}