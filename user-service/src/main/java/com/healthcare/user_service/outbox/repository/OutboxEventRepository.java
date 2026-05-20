package com.healthcare.user_service.outbox.repository;

import com.healthcare.user_service.outbox.constant.OutboxStatus;
import com.healthcare.user_service.outbox.model.OutboxEvent;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select e
            from OutboxEvent e
            where e.status = :status
            order by e.createdAt asc
            """)
    List<OutboxEvent> findTopForUpdateByStatus(
            @Param("status") OutboxStatus status,
            Pageable pageable
    );

    Optional<OutboxEvent> findByIdAndStatus(
            Long id,
            OutboxStatus status
    );

    long deleteByStatusAndPublishedAtBefore(
            OutboxStatus status,
            Instant publishedAt
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update OutboxEvent e
        set e.status = :newStatus,
            e.lastError = :lastError
        where e.status = :currentStatus
          and e.createdAt < :threshold
        """)
    int  resetStuckProcessingEvents(
            @Param("currentStatus") OutboxStatus currentStatus,
            @Param("newStatus") OutboxStatus newStatus,
            @Param("threshold") Instant threshold,
            @Param("lastError") String lastError
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
        update OutboxEvent e
        set e.createdAt = :createdAt
        where e.id = :id
        """)
    void updateCreatedAtById(
            @Param("id") Long id,
            @Param("createdAt") Instant createdAt
    );
}