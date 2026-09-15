package com.property.notification.repository;

import com.property.notification.domain.NotificationLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface NotificationLogRepository extends MongoRepository<NotificationLog, String> {
    List<NotificationLog> findByStatusAndAttemptsLessThan(NotificationLog.Status status, int maxAttempts);
}
