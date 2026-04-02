package com.healthcare.user_service.kafka.consumer;

import com.healthcare.user_service.kafka.event.UserRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.healthcare.user_service.kafka.constant.TopicName.USER_REGISTERED_V1;

@Slf4j
@Component
public class UserEventConsumer {

    @KafkaListener(topics = USER_REGISTERED_V1, groupId = "user-service-group")
    public void listen(UserRegisteredEvent event) {
        log.info("Received user.registered event: {}", event);
    }
}
