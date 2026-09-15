package com.apex.PropertyManagementService.repository;

import com.apex.PropertyManagementService.entity.Landlord;
import com.apex.PropertyManagementService.entity.enums.LandlordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LandlordRepository extends JpaRepository<Landlord, UUID> {

    /**
     * Find a landlord by email (unique field).
     */
    Optional<Landlord> findByEmail(String email);

    /**
     * Find all landlords with a specific status (e.g., ACTIVE, INACTIVE, SUSPENDED).
     */
    List<Landlord> findByStatus(LandlordStatus status);

    /**
     * Check if a landlord exists by email.
     */
    boolean existsByEmail(String email);
}