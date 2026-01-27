package com.healthcare.auth_service.config.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "header-request-id")
public record HeaderRequestIdProperties(
        @NotBlank
        String name
) {
}
