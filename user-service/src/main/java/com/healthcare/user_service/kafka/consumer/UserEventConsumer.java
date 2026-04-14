package com.healthcare.user_service.kafka.consumer;

import com.healthcare.user_service.kafka.event.UserDeletedEvent;
import com.healthcare.user_service.kafka.event.UserRegisteredEvent;
import com.healthcare.user_service.kafka.event.UserUpdatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserEventConsumer {

    @KafkaListener(
            topics = "${app.kafka.topics.user-registered.name}",
            groupId = "${app.kafka.groups.user-service.id}",
            containerFactory = "userRegisteredKafkaListenerContainerFactory"
    )
    public void listen(UserRegisteredEvent event) {
        log.info("Received user registered event: {}", event);
    }

    @KafkaListener(
            topics = "${app.kafka.topics.user-updated.name}",
            groupId = "${app.kafka.groups.user-service.id}",
            containerFactory = "userUpdatedKafkaListenerContainerFactory"
    )
    public void listen(UserUpdatedEvent event) {
        log.info("Received user updated event: {}", event);
    }

    @KafkaListener(
            topics = "${app.kafka.topics.user-deleted.name}",
            groupId = "${app.kafka.groups.user-service.id}",
            containerFactory = "userDeletedKafkaListenerContainerFactory"
    )
    public void listen(UserDeletedEvent event) {
        log.info("Received user deleted event: {}", event);
    }
}
