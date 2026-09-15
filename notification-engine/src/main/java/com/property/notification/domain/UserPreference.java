package com.property.notification.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "user_preferences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreference {
    @Id
    private String id;               // MongoDB ObjectId
    private Long userId;             // actual user ID (unique)
    private boolean emailOptIn;
    private boolean smsOptIn;
    private boolean inAppOptIn;
    private String email;
    private String phone;
}