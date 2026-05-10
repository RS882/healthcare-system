package com.healthcare.user_service.kafka.idempotency.service.interfacies;

import java.util.UUID;

public interface ProcessedEventService {

     boolean isProcessed(UUID eventId);

     void markProcessed(UUID eventId);
}
