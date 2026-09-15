package com.apex.PaymentService.module.SecurityDeposit.repository;

import com.apex.PaymentService.module.SecurityDeposit.entity.SecurityDeposit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SecurityDepositRepository extends JpaRepository<SecurityDeposit, UUID> {

    // Non‑paginated version (for uniqueness check)
    Optional<SecurityDeposit> findByLeaseId(UUID leaseId);

    // Paginated version (for listing)
    Page<SecurityDeposit> findByLeaseId(UUID leaseId, Pageable pageable);

    // Filter only active deposits (returnedDate is null)
    Page<SecurityDeposit> findByReturnedDateIsNull(Pageable pageable);
}
