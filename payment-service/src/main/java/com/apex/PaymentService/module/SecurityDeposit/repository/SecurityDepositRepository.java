package com.apex.PaymentService.module.SecurityDeposit.repository;

import com.apex.PaymentService.module.SecurityDeposit.entity.SecurityDeposit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SecurityDepositRepository extends JpaRepository<SecurityDeposit, UUID> {

    // ---- org-scoped lookups (HTTP path) ----

    Optional<SecurityDeposit> findByIdAndOrgId(UUID id, UUID orgId);

    boolean existsByIdAndOrgId(UUID id, UUID orgId);

    Optional<SecurityDeposit> findByOrgIdAndLeaseId(UUID orgId, UUID leaseId);

    Page<SecurityDeposit> findByOrgIdAndLeaseId(UUID orgId, UUID leaseId, Pageable pageable);

    Page<SecurityDeposit> findByOrgId(UUID orgId, Pageable pageable);

    Page<SecurityDeposit> findByOrgIdAndReturnedDateIsNull(UUID orgId, Pageable pageable);
}