package com.healthcare.user_service.kafka.config;

import com.healthcare.user_service.kafka.event.UserRegisteredEvent;
import com.healthcare.user_service.kafka.producer.UserEventProducer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Instant;
import java.util.UUID;

@Configuration

public class KafkaSmokeTestConfig {

    @Bean
    CommandLineRunner testKafka(UserEventProducer producer) {
        return args -> producer.send(
                new UserRegisteredEvent(
                        UUID.randomUUID(),
                        "USER_REGISTERED",
                        Instant.now(),
                        1L,
                        "test@example.com"
                )
        );
    }
}
