package com.healthcare.user_service.kafka.idempotency.repository;

import com.healthcare.user_service.kafka.idempotency.model.ProcessedEvent;
import com.healthcare.user_service.kafka.idempotency.model.ProcessedEventId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.UUID;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, ProcessedEventId> {

    long deleteByProcessedAtBefore(Instant threshold);

    @Modifying
    @Query("""
            update ProcessedEvent p
            set p.processedAt = :processedAt
            where p.id.eventId = :eventId
              and p.id.consumerName = :consumerName
            """)
    void updateProcessedAtById(
            UUID eventId,
            String consumerName,
            Instant processedAt
    );
}
