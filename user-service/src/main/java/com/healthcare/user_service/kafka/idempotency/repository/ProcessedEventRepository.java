package com.healthcare.user_service.kafka.idempotency.repository;

import com.healthcare.user_service.kafka.idempotency.model.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {
}
