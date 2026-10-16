package com.property.notification.repository;

import com.property.notification.domain.UserPreference;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserPreferenceRepository extends MongoRepository<UserPreference, String> {

    // ---- org-scoped HTTP paths ----

    Optional<UserPreference> findByOrganizationIdAndPublicId(UUID organizationId, UUID publicId);

    Optional<UserPreference> findByOrganizationIdAndEmail(UUID organizationId, String email);

    List<UserPreference> findByOrganizationIdAndSmsOptInTrue(UUID organizationId);

    List<UserPreference> findByOrganizationIdAndWhatsappOptInTrue(UUID organizationId);

    // ---- fallback for Kafka consumer (no TenantContext) ----

    Optional<UserPreference> findByPublicId(UUID publicId);
}
