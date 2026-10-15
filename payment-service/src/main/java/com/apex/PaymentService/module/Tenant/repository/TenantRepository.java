package com.apex.PaymentService.module.Tenant.repository;

import com.apex.PaymentService.module.Tenant.entity.Tenant;
import com.apex.PaymentService.module.Tenant.enums.TenantStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    // ---- org-scoped lookups (HTTP path) ----

    Optional<Tenant> findByIdAndOrgId(UUID id, UUID orgId);

    boolean existsByIdAndOrgId(UUID id, UUID orgId);

    Optional<Tenant> findByEmailAndOrgId(String email, UUID orgId);

    Page<Tenant> findByOrgId(UUID orgId, Pageable pageable);

    Page<Tenant> findByOrgIdAndStatus(UUID orgId, TenantStatus status, Pageable pageable);

    long countByOrgId(UUID orgId);
}
