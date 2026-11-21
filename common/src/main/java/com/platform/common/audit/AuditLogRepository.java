package com.platform.common.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    @Modifying
    @Query("DELETE FROM AuditLog a WHERE a.orgId = :orgId AND a.createdAt < :cutoff")
    int deleteOldByOrg(@Param("orgId") UUID orgId, @Param("cutoff") LocalDateTime cutoff);

    long countByOrgId(UUID orgId);

    @Query("SELECT DISTINCT a.orgId FROM AuditLog a")
    List<UUID> findDistinctOrgIds();
}
