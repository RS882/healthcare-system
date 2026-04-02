package com.healthcare.user_service.kafka.consumer;

import com.healthcare.user_service.kafka.event.UserRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserEventConsumer {

    @KafkaListener(
            topics = "${spring.kafka.topic.user-registered}",
            groupId = "${spring.kafka.consumer.user-service-group}"
    )
    public void listen(UserRegisteredEvent event) {
        log.info("Received : {}", event);
    }
}
