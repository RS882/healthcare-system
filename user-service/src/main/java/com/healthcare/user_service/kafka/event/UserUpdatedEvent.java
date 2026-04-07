package com.healthcare.user_service.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record UserUpdatedEvent(
        UUID eventId,
        EventType eventType,
        Instant occurredAt,
        Long userId,
        String email
) implements UserEvent {}
