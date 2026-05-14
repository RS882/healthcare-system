package com.healthcare.user_service.audit.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_log")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "event_id", nullable = false, unique = true, length = 36)
    private UUID eventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "aggregate_type", length = 100)
    private String aggregateType;

    @Column(name = "aggregate_id", length = 100)
    private String aggregateId;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    @Column(name = "payload")
    private String payload;

    @CreationTimestamp
    @Setter(AccessLevel.NONE)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}