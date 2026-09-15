package com.apex.auth.repository;

import com.apex.auth.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findTop100ByPublishedFalseOrderByCreatedAtAsc();

    @Modifying
    @Query("DELETE FROM OutboxEvent e WHERE e.published = true AND e.createdAt < :cutoff")
    int deleteByPublishedTrueAndCreatedAtBefore(@Param("cutoff") Instant cutoff);
}