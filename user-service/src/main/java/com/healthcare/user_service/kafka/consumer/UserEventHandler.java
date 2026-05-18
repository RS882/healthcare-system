package com.healthcare.user_service.kafka.consumer;

import com.healthcare.user_service.audit.service.interfacies.AuditService;
import com.healthcare.user_service.kafka.event.UserDeletedEvent;
import com.healthcare.user_service.kafka.event.UserEvent;
import com.healthcare.user_service.kafka.event.UserRegisteredEvent;
import com.healthcare.user_service.kafka.event.UserUpdatedEvent;
import com.healthcare.user_service.kafka.idempotency.service.interfacies.ProcessedEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventHandler {

    private final ProcessedEventService processedEventService;
    private final AuditService auditService;

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
        if (processedEventService.isProcessed(event.eventId())) {
            log.warn("Duplicate event skipped: eventId={}", event.eventId());
            return;
        }

        businessLogic.run();

        processedEventService.markProcessed(event.eventId());
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
