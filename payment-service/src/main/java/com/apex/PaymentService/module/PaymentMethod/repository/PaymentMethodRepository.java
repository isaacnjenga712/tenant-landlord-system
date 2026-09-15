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

    Optional<PaymentMethod> findByGatewayPaymentMethodId(String gatewayPaymentMethodId);

    Page<PaymentMethod> findByTenantId(UUID tenantId, Pageable pageable);

    Optional<PaymentMethod> findByTenantIdAndIsDefaultTrue(UUID tenantId);

    @Modifying
    @Query("UPDATE PaymentMethod p SET p.isDefault = false WHERE p.tenantId = :tenantId")
    void clearDefaultFlagForTenant(@Param("tenantId") UUID tenantId);
}