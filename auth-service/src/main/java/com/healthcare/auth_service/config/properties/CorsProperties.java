package com.healthcare.auth_service.config.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "cors")
public record CorsProperties(
        @NotBlank
        String allowedOrigins
) {
}
