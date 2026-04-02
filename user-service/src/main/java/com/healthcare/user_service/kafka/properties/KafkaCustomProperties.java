package com.healthcare.user_service.kafka.properties;


import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "spring.kafka")
public record KafkaCustomProperties(
        @NotBlank
        String bootstrapServers,
        Topic topic,
        Consumer consumer
) {

    public record Topic(
            @NotBlank
            String userRegistered
    ) {
    }

    public record Consumer(
            @NotBlank
            String userServiceGroup
    ) {
    }
}
