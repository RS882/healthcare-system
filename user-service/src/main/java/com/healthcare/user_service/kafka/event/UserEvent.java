package com.healthcare.user_service.kafka.event;

public sealed interface UserEvent extends DomainEvent permits
        UserRegisteredEvent,
        UserUpdatedEvent,
        UserDeletedEvent {

    Long userId();
}
