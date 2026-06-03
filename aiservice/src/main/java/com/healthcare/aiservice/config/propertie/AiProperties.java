package com.healthcare.aiservice.config.propertie;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai")
public record AiProperties(
        @NotBlank
        String provider,
        @NotBlank
        String model
) {
}
