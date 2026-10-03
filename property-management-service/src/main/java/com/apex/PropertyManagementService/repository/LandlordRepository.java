package com.apex.PropertyManagementService.repository;

import com.apex.PropertyManagementService.entity.Landlord;
import com.apex.PropertyManagementService.entity.enums.LandlordStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LandlordRepository extends JpaRepository<Landlord, UUID> {

    Optional<Landlord> findByEmail(String email);

    Optional<Landlord> findByUserId(UUID userId);   // ← ADD THIS

    List<Landlord> findByStatus(LandlordStatus status);

    boolean existsByEmail(String email);
}