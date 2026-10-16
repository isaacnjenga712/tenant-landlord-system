package com.property.notification.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document(collection = "user_preferences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndex(
        name = "org_publicid_idx",
        def = "{'organizationId': 1, 'publicId': 1}",
        unique = true)
public class UserPreference {

    @Id
    private String id;

    /** SaaS tenant (org) that owns this user record. */
    @Indexed
    private UUID organizationId;

    /**
     * The user's publicId from auth-service. Matches JWT "publicId" claim
     * and recipient IDs carried in event payloads.
     */
    @Indexed
    private UUID publicId;

    private String fullName;
    private String email;
    private String phone;
    private String whatsappNumber;

    private boolean emailOptIn;
    private boolean smsOptIn;
    private boolean inAppOptIn;
    private boolean whatsappOptIn;
}