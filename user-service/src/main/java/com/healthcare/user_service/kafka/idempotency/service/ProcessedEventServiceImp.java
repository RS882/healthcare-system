package com.healthcare.user_service.kafka.idempotency.service;

import com.healthcare.user_service.kafka.idempotency.model.ProcessedEvent;
import com.healthcare.user_service.kafka.idempotency.model.ProcessedEventId;
import com.healthcare.user_service.kafka.idempotency.repository.ProcessedEventRepository;
import com.healthcare.user_service.kafka.idempotency.service.interfacies.ProcessedEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProcessedEventServiceImp implements ProcessedEventService {

    private final ProcessedEventRepository repository;

    @Override
    public boolean isProcessed(UUID eventId, String consumerName) {
        return repository.existsById(
                new ProcessedEventId(eventId, consumerName)
        );
    }

    @Override
    public void markProcessed(UUID eventId, String consumerName) {
        repository.save(
                ProcessedEvent.builder()
                        .id(new ProcessedEventId(eventId, consumerName))
                        .build()
        );
    }
}