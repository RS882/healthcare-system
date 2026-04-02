package com.healthcare.user_service.kafka.producer;

import com.healthcare.user_service.kafka.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static com.healthcare.user_service.kafka.constant.TopicName.USER_REGISTERED_V1;

@Component
@RequiredArgsConstructor
public class UserEventProducer {

    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;

    public void send(UserRegisteredEvent event) {
        kafkaTemplate.send(USER_REGISTERED_V1, event.userId().toString(), event);
    }
}
