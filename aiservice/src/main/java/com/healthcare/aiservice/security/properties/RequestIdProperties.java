package com.healthcare.aiservice.security.properties;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "request-id")
public record RequestIdProperties(
        @NotBlank(message = "Request ID prefix must not be blank.")
        String prefix,

        @NotNull(message = "Request ID ttl must not be null.")
        @DurationMin(seconds = 1, message = "Request ID ttl must be at least 1s")
        Duration ttl,

        @NotBlank(message = "Request ID value must not be blank.")
        String value
) {
}
