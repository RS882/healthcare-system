package com.healthcare.auth_service.config.properties;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "request-id")
public record RequestIdProperties(
        @NotBlank
        String prefix,

        @NotNull
        @DurationMin(seconds = 1)
        Duration ttl,

        @NotBlank
        String value
) {
}
