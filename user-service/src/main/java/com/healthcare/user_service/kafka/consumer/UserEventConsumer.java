package com.healthcare.user_service.kafka.consumer;

import com.healthcare.user_service.kafka.event.UserDeletedEvent;
import com.healthcare.user_service.kafka.event.UserRegisteredEvent;
import com.healthcare.user_service.kafka.event.UserUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventConsumer {

    private final UserEventHandler userEventHandler;

    @KafkaListener(
            topics = "${app.kafka.topics.user-registered.name}",
            groupId = "${app.kafka.groups.user-service.id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(UserRegisteredEvent event) {
        userEventHandler.handle(event);
    }

    @KafkaListener(
            topics = "${app.kafka.topics.user-updated.name}",
            groupId = "${app.kafka.groups.user-service.id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(UserUpdatedEvent event) {
        userEventHandler.handle(event);
    }

    @KafkaListener(
            topics = "${app.kafka.topics.user-deleted.name}",
            groupId = "${app.kafka.groups.user-service.id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(UserDeletedEvent event) {
        userEventHandler.handle(event);
    }
}