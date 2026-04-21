package com.healthcare.user_service.outbox.service.intrefacies;

import com.healthcare.user_service.kafka.event.DomainEvent;

public interface OutboxEventService {
    void save(DomainEvent event);
}
