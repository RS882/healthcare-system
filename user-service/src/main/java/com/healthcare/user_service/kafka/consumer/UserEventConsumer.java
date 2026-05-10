package com.healthcare.user_service.kafka.consumer;

import com.healthcare.user_service.audit.service.interfacies.AuditService;
import com.healthcare.user_service.kafka.event.UserDeletedEvent;
import com.healthcare.user_service.kafka.event.UserEvent;
import com.healthcare.user_service.kafka.event.UserRegisteredEvent;
import com.healthcare.user_service.kafka.event.UserUpdatedEvent;
import com.healthcare.user_service.kafka.idempotency.service.interfacies.ProcessedEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventConsumer {

    private final ProcessedEventService processedEventService;
    private final AuditService auditService;

    @KafkaListener(
            topics = "${app.kafka.topics.user-registered.name}",
            groupId = "${app.kafka.groups.user-service.id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(UserRegisteredEvent event) {

        handle(event, () -> handleUserRegistered(event));
    }

    @KafkaListener(
            topics = "${app.kafka.topics.user-updated.name}",
            groupId = "${app.kafka.groups.user-service.id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(UserUpdatedEvent event) {

        handle(event, () -> handleUserUpdated(event));
    }

    @KafkaListener(
            topics = "${app.kafka.topics.user-deleted.name}",
            groupId = "${app.kafka.groups.user-service.id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(UserDeletedEvent event) {

        handle(event, () -> handleUserDeleted(event));
    }

    private void handle(UserEvent event, Runnable businessLogic) {
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