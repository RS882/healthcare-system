package com.healthcare.user_service.kafka.properties;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.kafka")
public record KafkaCustomProperties(
        @NotBlank
        String bootstrapServers,
        Topics topics,
        Groups groups
) {

    public record Topics(
            @NotNull
            TopicProperties userRegistered,
            @NotNull
            TopicProperties userUpdated,
            @NotNull
            TopicProperties userDeleted
    ) {
    }

    public record TopicProperties(
            @NotBlank
            String name,
            @Positive
            int partitions,
            @Positive
            short replicas
    ) {
    }

    public record Groups(
            @NotNull
            GroupProperties userService
    ) {
    }

    public record GroupProperties(
            @NotBlank
            String id
    ) {
    }
}
