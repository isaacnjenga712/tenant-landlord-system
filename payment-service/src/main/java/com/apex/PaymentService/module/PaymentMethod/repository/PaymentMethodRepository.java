package com.apex.PaymentService.module.PaymentMethod.repository;

import com.apex.PaymentService.module.PaymentMethod.entity.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, UUID> {

    // ---- org-scoped lookups (HTTP path) ----

    Optional<PaymentMethod> findByIdAndOrgId(UUID id, UUID orgId);

    boolean existsByIdAndOrgId(UUID id, UUID orgId);

    Optional<PaymentMethod> findByOrgIdAndGatewayPaymentMethodId(UUID orgId, String gatewayPaymentMethodId);

    Page<PaymentMethod> findByOrgIdAndTenantId(UUID orgId, UUID tenantId, Pageable pageable);

    Optional<PaymentMethod> findByOrgIdAndTenantIdAndIsDefaultTrue(UUID orgId, UUID tenantId);

    @Modifying
    @Query("UPDATE PaymentMethod p SET p.isDefault = false WHERE p.orgId = :orgId AND p.tenantId = :tenantId")
    void clearDefaultFlagForOrgAndTenant(@Param("orgId") UUID orgId, @Param("tenantId") UUID tenantId);
}