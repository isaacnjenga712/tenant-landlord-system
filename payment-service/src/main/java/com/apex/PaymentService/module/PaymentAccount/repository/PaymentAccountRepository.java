package com.apex.PaymentService.module.PaymentAccount.repository;

import com.apex.PaymentService.module.PaymentAccount.entity.PaymentAccount;
import com.apex.PaymentService.module.PaymentAccount.enums.EntityType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PaymentAccountRepository extends JpaRepository<PaymentAccount, UUID> {

    // ---- org-scoped lookups (HTTP path) ----

    Optional<PaymentAccount> findByIdAndOrgId(UUID id, UUID orgId);

    boolean existsByIdAndOrgId(UUID id, UUID orgId);

    Page<PaymentAccount> findByOrgIdAndEntityType(UUID orgId, EntityType entityType, Pageable pageable);

    Page<PaymentAccount> findByOrgId(UUID orgId, Pageable pageable);

    Optional<PaymentAccount> findByOrgIdAndEntityTypeAndEntityId(UUID orgId, EntityType entityType, UUID entityId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PaymentAccount p WHERE p.id = :id AND p.orgId = :orgId AND p.isActive = true")
    Optional<PaymentAccount> findByIdAndOrgIdWithLock(@Param("id") UUID id, @Param("orgId") UUID orgId);
}