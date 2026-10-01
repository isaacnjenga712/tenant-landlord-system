package com.property.notification.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Legacy recovery scheduler — disabled.
 *
 * The new ChannelDispatcher handles per-attempt logging inline.
 * A future iteration will retry failed rows from notification_logs
 * using the NotificationChannel abstraction; the old implementation
 * depended on Long userId + direct service calls, which no longer apply.
 *
 * Set app.notification.retry-scheduler.enabled=true to (re)enable when
 * the new retry implementation lands.
 */
@Component
@ConditionalOnProperty(
        name = "app.notification.retry-scheduler.enabled",
        havingValue = "true",
        matchIfMissing = false
)
@Slf4j
public class NotificationRecoveryScheduler {

    public NotificationRecoveryScheduler() {
        log.warn("NotificationRecoveryScheduler is enabled but unimplemented (no-op).");
    }
}
