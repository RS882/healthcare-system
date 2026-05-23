package com.healthcare.user_service.kafka.consumer;

import com.healthcare.user_service.kafka.event.UserDeletedEvent;
import com.healthcare.user_service.kafka.event.UserRegisteredEvent;
import com.healthcare.user_service.kafka.event.UserUpdatedEvent;
import com.healthcare.user_service.kafka.properties.KafkaCustomProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Getter
@Component
@RequiredArgsConstructor
public class UserEventConsumer {

    private final UserEventHandler userEventHandler;
    private final KafkaCustomProperties props;

    @KafkaListener(
            topics = "#{__listener.props.topics().userRegistered().name()}",
            groupId = "#{__listener.props.groups().userService().id()}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(UserRegisteredEvent event) {
        userEventHandler.handle(event);
    }

    @KafkaListener(
            topics = "#{__listener.props.topics().userUpdated().name()}",
            groupId = "#{__listener.props.groups().userService().id()}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(UserUpdatedEvent event) {
        userEventHandler.handle(event);
    }

    @KafkaListener(
            topics = "#{__listener.props.topics().userDeleted().name()}",
            groupId = "#{__listener.props.groups().userService().id()}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(UserDeletedEvent event) {
        userEventHandler.handle(event);
    }
}