package com.healthcare.user_service.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record UserDeletedEvent(
        UUID eventId,
        EventType eventType,
        Instant occurredAt,
        Long userId,
        String email
) implements UserEvent {
    public static UserDeletedEvent of(Long userId, String email) {
        return new UserDeletedEvent(
                UUID.randomUUID(),
                EventType.USER_DELETED,
                Instant.now(),
                userId,
                email
        );
    }
}
