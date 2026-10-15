package com.apex.PaymentService.module.DepositDeduction.repository;

import com.apex.PaymentService.module.DepositDeduction.entity.DepositDeduction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DepositDeductionRepository extends JpaRepository<DepositDeduction, UUID> {

    // ---- org-scoped lookups (HTTP path) ----

    Optional<DepositDeduction> findByIdAndOrgId(UUID id, UUID orgId);

    List<DepositDeduction> findByOrgIdAndDepositId(UUID orgId, UUID depositId);

    Page<DepositDeduction> findByOrgIdAndDepositId(UUID orgId, UUID depositId, Pageable pageable);

    void deleteByOrgIdAndDepositId(UUID orgId, UUID depositId);
}