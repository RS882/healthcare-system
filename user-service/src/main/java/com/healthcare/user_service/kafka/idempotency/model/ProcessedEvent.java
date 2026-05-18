package com.healthcare.user_service.kafka.idempotency.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "processed_event")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedEvent {

    @EmbeddedId
    private ProcessedEventId id;

    @CreationTimestamp
    @Column(name = "processed_at", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Instant processedAt;
}