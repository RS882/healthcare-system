package com.healthcare.user_service.kafka.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "spring.kafka.bootstrap-servers")
public record KafkaBootstrapServersProperties(
        @NotBlank
        String bootstrapServers
) {
}
