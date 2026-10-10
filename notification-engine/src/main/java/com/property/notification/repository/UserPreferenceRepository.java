package com.property.notification.repository;

import com.property.notification.domain.UserPreference;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserPreferenceRepository extends MongoRepository<UserPreference, String> {

    Optional<UserPreference> findByPublicId(UUID publicId);

    Optional<UserPreference> findByEmail(String email);

    List<UserPreference> findBySmsOptInTrue();

    List<UserPreference> findByWhatsappOptInTrue();
}
