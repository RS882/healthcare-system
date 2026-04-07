package com.healthcare.user_service.kafka.producer;

import com.healthcare.user_service.kafka.event.UserEvent;
import com.healthcare.user_service.kafka.properties.KafkaCustomProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class UserEventProducer {

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;
    private final KafkaCustomProperties kafkaCustomProperties;

    public void send(UserEvent event) {
        kafkaTemplate.send(
                kafkaCustomProperties.topics().userRegistered().name(),
                event.userId().toString(),
                event);
    }
}
