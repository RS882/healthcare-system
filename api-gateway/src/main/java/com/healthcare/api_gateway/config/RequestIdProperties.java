package com.healthcare.api_gateway.config;


import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "request-id")
public record RequestIdProperties(
        @NotBlank String prefix,
        @NotBlank Duration ttl,
        @NotBlank String value
) {
}
