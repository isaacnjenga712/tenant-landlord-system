package com.property.notification.config;

import com.property.notification.domain.Notification;
import com.property.notification.domain.NotificationLog;
import com.property.notification.domain.UserPreference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile("backfill")
@RequiredArgsConstructor
@Slf4j
public class TenantBackfillRunner implements CommandLineRunner {

    private static final UUID PLACEHOLDER_ORG =
            UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final MongoTemplate mongo;

    @Override
    public void run(String... args) {
        log.warn("### Running tenant backfill for notification-engine ###");

        Query missing = Query.query(Criteria.where("organizationId").is(null));
        Update set = new Update().set("organizationId", PLACEHOLDER_ORG);

        long n = mongo.updateMulti(missing, set, Notification.class).getModifiedCount();
        log.warn("### notifications backfilled: {}", n);

        long l = mongo.updateMulti(missing, set, NotificationLog.class).getModifiedCount();
        log.warn("### notification_logs backfilled: {}", l);

        long p = mongo.updateMulti(missing, set, UserPreference.class).getModifiedCount();
        log.warn("### user_preferences backfilled: {}", p);

        log.warn("### Backfill complete. Restart without 'backfill' profile.");
    }
}
