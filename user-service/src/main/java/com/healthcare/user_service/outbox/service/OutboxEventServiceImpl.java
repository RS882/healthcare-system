package com.healthcare.user_service.outbox.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.user_service.exception_handler.exception.SerializeEventException;
import com.healthcare.user_service.kafka.event.DomainEvent;
import com.healthcare.user_service.kafka.properties.KafkaCustomProperties;
import com.healthcare.user_service.outbox.model.OutboxEvent;
import com.healthcare.user_service.outbox.repository.OutboxEventRepository;
import com.healthcare.user_service.outbox.service.intrefacies.OutboxEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.healthcare.user_service.outbox.mapper.OutboxEventMapper.toBasicOutboxEvent;

@Service
@RequiredArgsConstructor
public class OutboxEventServiceImpl implements OutboxEventService {

    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;
    private final KafkaCustomProperties kafkaProperties;

    @Override
    public void save(DomainEvent event) {

        OutboxEvent outboxEvent = toBasicOutboxEvent(event);

        outboxEvent.setTopic(resolveTopic(event));
        outboxEvent.setPayload(serialize(event));

        repository.save(outboxEvent);
    }

    private String resolveTopic(DomainEvent event) {
        return switch (event.eventType()) {
            case USER_REGISTERED -> kafkaProperties.topics().userRegistered().name();
            case USER_UPDATED -> kafkaProperties.topics().userUpdated().name();
            case USER_DELETED -> kafkaProperties.topics().userDeleted().name();
        };
    }

    private String serialize(DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new SerializeEventException( event.eventType().name(), e);
        }
    }
}
