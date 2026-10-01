package com.property.notification.service.channel;

import com.property.notification.domain.UserPreference;

public interface NotificationChannel {

    /** Short name used in logs and NotificationLog.channel. */
    String name();

    /** Whether the user has opted in to this channel. */
    boolean isOptedIn(UserPreference prefs);

    /**
     * Deliver the notification.
     *
     * @param prefs      recipient preferences (contains email / phone / whatsapp)
     * @param subject    short subject (email) or ignored for SMS/WhatsApp
     * @param body       message body
     */
    void send(UserPreference prefs, String subject, String body) throws Exception;
}
