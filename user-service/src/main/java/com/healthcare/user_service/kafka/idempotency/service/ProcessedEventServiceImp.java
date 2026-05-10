package com.healthcare.user_service.kafka.idempotency.service;

import com.healthcare.user_service.kafka.idempotency.model.ProcessedEvent;
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
    public boolean isProcessed(UUID eventId) {
        return repository.existsById(eventId);
    }

    @Override
    public void markProcessed(UUID eventId) {
        repository.save(
                ProcessedEvent.builder()
                        .eventId(eventId)
                        .build()
        );
    }
}