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

    /**
     * Finds all payment accounts for a given entity type with pagination.
     *
     * @param entityType the type of entity (tenant, landlord, property)
     * @param pageable   pagination information
     * @return a page of PaymentAccount entities
     */
    Page<PaymentAccount> findByEntityType(EntityType entityType, Pageable pageable);

    /**
     * Finds a payment account by its entity type and entity ID.
     * Used to check for duplicate accounts and to retrieve accounts by entity.
     *
     * @param entityType the type of entity
     * @param entityId   the UUID of the entity
     * @return an Optional containing the PaymentAccount if found
     */
    Optional<PaymentAccount> findByEntityTypeAndEntityId(EntityType entityType, UUID entityId);

    /**
     * Finds a payment account by ID with a pessimistic write lock.
     * Used for balance updates to prevent race conditions.
     *
     * @param id the account UUID
     * @return an Optional containing the locked PaymentAccount
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PaymentAccount p WHERE p.id = :id AND p.isActive = true")
    Optional<PaymentAccount> findByIdWithLock(@Param("id") UUID id);
}