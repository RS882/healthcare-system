package com.healthcare.user_service.kafka.idempotency.service.interfacies;

import java.util.UUID;

public interface ProcessedEventService {

     boolean isProcessed(UUID eventId, String consumerName);

     void markProcessed(UUID eventId, String consumerName);
}
