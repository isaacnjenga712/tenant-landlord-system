package com.property.notification.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document(collection = "user_preferences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreference {

    @Id
    private String id;

    /**
     * The user's publicId from auth-service. Matches JWT "publicId" claim
     * and any tenantId/landlordId carried in event payloads.
     */
    @Indexed(unique = true)
    private UUID publicId;

    private String fullName;
    private String email;
    private String phone;            // E.164, e.g. +254757380426
    private String whatsappNumber;   // E.164, may differ from phone

    private boolean emailOptIn;
    private boolean smsOptIn;
    private boolean inAppOptIn;
    private boolean whatsappOptIn;
}