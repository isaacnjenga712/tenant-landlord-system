package com.apex.module.lease.repository;

import com.apex.module.lease.entity.Lease;
import com.apex.module.lease.enums.LeaseStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


public interface LeaseRepository extends JpaRepository<Lease, UUID>, JpaSpecificationExecutor<Lease> {
    List<Lease> findByTenantId(UUID tenantId);
    List<Lease> findByLandlordId(UUID landlordId);
    List<Lease> findByPropertyId(UUID propertyId);
    List<Lease> findByStatus(LeaseStatus status);  // but we might need to import LeaseStatus

    @Query("SELECT l FROM Lease l WHERE l.propertyId = :propertyId AND " +
           "(l.startDate <= :endDate AND l.endDate >= :startDate)")
    List<Lease> findOverlappingLeases(@Param("propertyId") UUID propertyId,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate);
}