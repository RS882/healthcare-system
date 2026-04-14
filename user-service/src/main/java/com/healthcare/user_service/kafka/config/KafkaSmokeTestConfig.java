package com.healthcare.user_service.kafka.config;

import com.healthcare.user_service.kafka.event.EventType;
import com.healthcare.user_service.kafka.event.UserRegisteredEvent;
import com.healthcare.user_service.kafka.producer.UserEventProducer;
import com.healthcare.user_service.kafka.service.KafkaKeyMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class KafkaSmokeTestConfig {

    private final KafkaKeyMessageService keyMessageService;

    @Bean
    CommandLineRunner testKafka(UserEventProducer producer) {

        UUID uuid = UUID.randomUUID();
        keyMessageService.getKeys().add(uuid);

        return args -> producer.send(
                new UserRegisteredEvent(
                        uuid,
                        EventType.USER_REGISTERED,
                        Instant.now(),
                        1L,
                        "test@example.com"
                )
        );
    }
}
