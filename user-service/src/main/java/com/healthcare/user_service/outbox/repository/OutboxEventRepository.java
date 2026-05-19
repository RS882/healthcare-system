package com.healthcare.user_service.outbox.repository;

import com.healthcare.user_service.outbox.constant.OutboxStatus;
import com.healthcare.user_service.outbox.model.OutboxEvent;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}