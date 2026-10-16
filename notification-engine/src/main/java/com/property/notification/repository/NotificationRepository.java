package com.property.notification.repository;

import com.property.notification.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends MongoRepository<Notification, String> {

    // ---- org-scoped HTTP paths ----

    Page<Notification> findByOrganizationIdAndRecipientPublicIdOrderByCreatedAtDesc(
            UUID organizationId, UUID recipientPublicId, Pageable pageable);

    long countByOrganizationIdAndRecipientPublicIdAndReadIsFalse(
            UUID organizationId, UUID recipientPublicId);

    Optional<Notification> findByIdAndOrganizationId(String id, UUID organizationId);

    @Query("{ '_id': ?0, 'organizationId': ?1, 'recipientPublicId': ?2, 'read': false }")
    @Update("{ '$set': { 'read': true, 'readAt': ?3 } }")
    long markReadInOrg(String id, UUID organizationId, UUID recipientPublicId, LocalDateTime readAt);

    @Query("{ 'organizationId': ?0, 'recipientPublicId': ?1, 'read': false }")
    @Update("{ '$set': { 'read': true, 'readAt': ?2 } }")
    long markAllReadInOrg(UUID organizationId, UUID recipientPublicId, LocalDateTime readAt);

    // ---- un-scoped: only used by backfill runner ----

    @Query("{ 'organizationId': null }")
    Page<Notification> findWithoutOrganization(Pageable pageable);
}