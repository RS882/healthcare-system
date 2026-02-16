package com.healthcare.api_gateway.config.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "user-context")
public record UserContextProperties(
        @NotBlank
        String userContextHeader,

        @NotNull
        @DurationMin(seconds = 1)
        Duration ttl

) {
}
