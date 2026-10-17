package com.apex.maintenanceTicketService.repository;

import com.apex.maintenanceTicketService.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    Optional<Tenant> findByIdAndOrgId(UUID id, UUID orgId);

    Optional<Tenant> findByOrgIdAndEmail(UUID orgId, String email);

    boolean existsByOrgIdAndEmail(UUID orgId, String email);
}
