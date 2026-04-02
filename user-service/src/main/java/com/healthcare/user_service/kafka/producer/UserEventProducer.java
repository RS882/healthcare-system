package com.healthcare.user_service.kafka.producer;

import com.healthcare.user_service.kafka.event.UserRegisteredEvent;
import com.healthcare.user_service.kafka.properties.KafkaCustomProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;



@Component
@RequiredArgsConstructor
public class UserEventProducer {

    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;
    private final KafkaCustomProperties kafkaCustomProperties;

    public void send(UserRegisteredEvent event) {
        kafkaTemplate.send(kafkaCustomProperties.topic().userRegistered(), event.userId().toString(), event);
    }
}
