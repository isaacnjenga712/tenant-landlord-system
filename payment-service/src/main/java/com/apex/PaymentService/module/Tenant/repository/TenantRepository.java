package com.apex.PaymentService.module.Tenant.repository;

import com.apex.PaymentService.module.Tenant.entity.Tenant;
import com.apex.PaymentService.module.Tenant.enums.TenantStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    Optional<Tenant> findByEmail(String email);
    Page<Tenant> findByStatus(TenantStatus status, Pageable pageable);
}
