package com.healthcare.user_service.outbox.model;

import com.healthcare.user_service.constant.Role;
import com.healthcare.user_service.outbox.constant.OutboxStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_event")
@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@ToString
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "event_id", nullable = false, unique = true, length = 36)
    private UUID eventId;

    @Column(name ="aggregate_type", nullable = false)
    private String aggregateType;

    @Column(name ="aggregate_id", nullable = false)
    private String aggregateId;

    @Column(name ="topic", nullable = false)
    private String topic;

    @Column(name ="event_type", nullable = false)
    private String eventType;

    @Column(name ="payload", nullable = false)
    private String payload;

    @Column(name ="occurred_at", nullable = false)
    private Instant occurredAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Instant createdAt;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OutboxStatus status;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "published_at")
    private Instant publishedAt;
}
