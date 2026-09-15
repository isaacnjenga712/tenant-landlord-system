package com.apex.leaseService.repository;

import com.apex.leaseService.entity.Lease;
import com.apex.leaseService.entity.LeaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface LeaseRepository extends JpaRepository<Lease, UUID> {

    List<Lease> findByUnitId(UUID unitId);

    // Query through the tenants collection
    @Query("SELECT l FROM Lease l JOIN l.tenants t WHERE t.id = :tenantId")
    List<Lease> findByTenantId(@Param("tenantId") UUID tenantId);

    // ✅ NEW: Find leases by property ID
    List<Lease> findByPropertyId(UUID propertyId);

    List<Lease> findByStatus(LeaseStatus status);

    @Query("SELECT l FROM Lease l WHERE l.endDate BETWEEN :start AND :end AND l.status = 'ACTIVE'")
    List<Lease> findActiveLeasesExpiringBetween(@Param("start") LocalDate start, 
                                                @Param("end") LocalDate end);
}