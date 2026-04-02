package com.healthcare.user_service.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record UserRegisteredEvent(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        Long userId,
        String email
) {}
