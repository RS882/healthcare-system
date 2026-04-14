package com.healthcare.user_service.kafka.producer;

import com.healthcare.user_service.kafka.event.DomainEvent;
import com.healthcare.user_service.kafka.properties.KafkaCustomProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class UserEventProducer {

    private final KafkaTemplate<String, DomainEvent> kafkaTemplate;
    private final KafkaCustomProperties kafkaCustomProperties;

    public void send(DomainEvent event) {
        kafkaTemplate.send(
                kafkaCustomProperties.topics().userRegistered().name(),
                event.eventId().toString(),
                event);
    }
}
