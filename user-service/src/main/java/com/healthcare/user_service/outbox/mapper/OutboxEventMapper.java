package com.healthcare.user_service.outbox.mapper;

import com.healthcare.user_service.exception_handler.exception.UnsupportedEventTypeException;
import com.healthcare.user_service.kafka.event.DomainEvent;
import com.healthcare.user_service.kafka.event.UserEvent;
import com.healthcare.user_service.outbox.model.OutboxEvent;

import static com.healthcare.user_service.outbox.constant.AggregateType.AGGREGATE_TYPE_USER;

public final class OutboxEventMapper {
    private OutboxEventMapper() {
    }

    public static OutboxEvent toBasicOutboxEvent(DomainEvent event) {

        OutboxEvent outboxEvent = OutboxEvent.builder()
                .eventId(event.eventId())
                .eventType(event.eventType().name())
                .occurredAt(event.occurredAt())
                .published(false)
                .build();

        if (event instanceof UserEvent userEvent) {
            outboxEvent.setAggregateType(AGGREGATE_TYPE_USER.value());
            outboxEvent.setAggregateId(String.valueOf(userEvent.userId()));
        } else {
            throw new UnsupportedEventTypeException(event.getClass().getName());
        }

        return outboxEvent;
    }
}
