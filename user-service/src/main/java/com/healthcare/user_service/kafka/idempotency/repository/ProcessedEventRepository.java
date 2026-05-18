package com.healthcare.user_service.kafka.idempotency.repository;

import com.healthcare.user_service.kafka.idempotency.model.ProcessedEvent;
import com.healthcare.user_service.kafka.idempotency.model.ProcessedEventId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, ProcessedEventId> {
}
