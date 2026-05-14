package com.healthcare.user_service.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.user_service.audit.model.AuditLog;
import com.healthcare.user_service.audit.repository.AuditLogRepository;

import com.healthcare.user_service.audit.resolver.CompositeAuditAggregateResolver;
import com.healthcare.user_service.audit.resolver.dto.AuditAggregate;
import com.healthcare.user_service.audit.service.interfacies.AuditService;
import com.healthcare.user_service.exception_handler.exception.SerializeEventException;
import com.healthcare.user_service.kafka.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository repository;
    private final ObjectMapper objectMapper;
    private final CompositeAuditAggregateResolver aggregateResolver;

    @Override
    public void recordEvent(DomainEvent event) {
        AuditAggregate aggregate = aggregateResolver.resolve(event);

        AuditLog auditLog = AuditLog.builder()
                .eventId(event.eventId())
                .eventType(event.eventType().name())
                .aggregateType(aggregate.aggregateType())
                .aggregateId(aggregate.aggregateId())
                .message(buildMessage(event))
                .payload(serialize(event))
                .build();

        repository.save(auditLog);
    }

    private String buildMessage(DomainEvent event) {
        return switch (event.eventType()) {
            case USER_REGISTERED -> "User registered";
            case USER_UPDATED -> "User updated";
            case USER_DELETED -> "User deleted";
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