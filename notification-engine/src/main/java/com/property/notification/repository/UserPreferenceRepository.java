package com.property.notification.repository;

import com.property.notification.domain.UserPreference;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UserPreferenceRepository extends MongoRepository<UserPreference, String> {
    Optional<UserPreference> findByUserId(Long userId);
}
