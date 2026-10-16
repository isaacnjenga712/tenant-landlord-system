package com.property.notification.repository;

import com.property.notification.domain.NotificationLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationLogRepository extends MongoRepository<NotificationLog, String> {

    // System scheduler — spans all orgs by design, no TenantContext.
    List<NotificationLog> findByStatusAndAttemptsLessThan(NotificationLog.Status status, int maxAttempts);

    List<NotificationLog> findByOrganizationIdAndStatusAndAttemptsLessThan(
            UUID organizationId, NotificationLog.Status status, int maxAttempts);

    org.springframework.data.domain.Page<NotificationLog> findByOrganizationId(
            UUID organizationId, org.springframework.data.domain.Pageable pageable);
}