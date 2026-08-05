package com.healthcare.aiservice.security.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "header-request-id")
public record HeaderRequestIdProperties(
        @NotBlank(message = "Header request ID name must not be blank.")
        String name
) {
}
