package com.healthcare.user_service.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record UserRegisteredEvent(
        UUID eventId,
        EventType eventType,
        Instant occurredAt,
        Long userId,
        String email
) implements UserEvent {

    public static UserRegisteredEvent of(Long userId, String email) {
        return new UserRegisteredEvent(
                UUID.randomUUID(),
                EventType.USER_REGISTERED,
                Instant.now(),
                userId,
                email
        );
    }
}
