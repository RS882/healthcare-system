package com.healthcare.user_service.kafka.consumer;

import com.healthcare.user_service.audit.service.interfacies.AuditService;
import com.healthcare.user_service.kafka.event.UserDeletedEvent;
import com.healthcare.user_service.kafka.event.UserEvent;
import com.healthcare.user_service.kafka.event.UserRegisteredEvent;
import com.healthcare.user_service.kafka.event.UserUpdatedEvent;
import com.healthcare.user_service.kafka.idempotency.service.interfacies.ProcessedEventService;
import com.healthcare.user_service.kafka.metrics.KafkaConsumerMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.healthcare.user_service.kafka.consumer.ConsumerNames.USER_EVENT_CONSUMER;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventHandler {

    private final ProcessedEventService processedEventService;
    private final AuditService auditService;
    private final KafkaConsumerMetricsService metricsService;

    @Transactional
    public void handle(UserRegisteredEvent event) {
        handleInternal(event, () -> handleUserRegistered(event));
    }

    @Transactional
    public void handle(UserUpdatedEvent event) {
        handleInternal(event, () -> handleUserUpdated(event));
    }

    @Transactional
    public void handle(UserDeletedEvent event) {
        handleInternal(event, () -> handleUserDeleted(event));
    }

    private void handleInternal(UserEvent event, Runnable businessLogic) {
        if (processedEventService.isProcessed(event.eventId(), USER_EVENT_CONSUMER)) {
            metricsService.incrementDuplicate();
            log.warn("Duplicate event skipped: eventId={}", event.eventId());
            return;
        }

        try {
            businessLogic.run();
            processedEventService.markProcessed(event.eventId(), USER_EVENT_CONSUMER);
            metricsService.incrementProcessed();
        } catch (Exception e) {
            metricsService.incrementFailed();
            throw e;
        }
    }

    private void handleUserRegistered(UserRegisteredEvent event) {
        log.info("Handle user registered: userId={}, email={}", event.userId(), event.email());
        auditService.recordEvent(event);
        // TODO: welcome notification / analytics
    }

    private void handleUserUpdated(UserUpdatedEvent event) {
        log.info("Handle user updated: userId={}", event.userId());
        auditService.recordEvent(event);
        // TODO: update projection
    }

    private void handleUserDeleted(UserDeletedEvent event) {
        log.info("Handle user deleted: userId={}", event.userId());
        auditService.recordEvent(event);
        // TODO: cleanup
    }
}
