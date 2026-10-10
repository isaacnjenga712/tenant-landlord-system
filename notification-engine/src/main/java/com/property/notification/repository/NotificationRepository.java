package com.property.notification.repository;

import com.property.notification.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import java.time.LocalDateTime;
import java.util.UUID;

public interface NotificationRepository extends MongoRepository<Notification, String> {

    Page<Notification> findByRecipientPublicIdOrderByCreatedAtDesc(
            UUID recipientPublicId, Pageable pageable);

    long countByRecipientPublicIdAndReadIsFalse(UUID recipientPublicId);

    @Query("{ 'recipientPublicId': ?0, 'read': false }")
    @Update("{ '$set': { 'read': true, 'readAt': ?1 } }")
    long markAllRead(UUID recipientPublicId, LocalDateTime readAt);
}
