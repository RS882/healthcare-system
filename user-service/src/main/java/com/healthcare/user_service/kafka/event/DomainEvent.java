package com.healthcare.user_service.kafka.event;

import java.time.Instant;
import java.util.UUID;

public sealed interface DomainEvent permits
        UserEvent {

    UUID eventId();

    EventType eventType();

    Instant occurredAt();
}
