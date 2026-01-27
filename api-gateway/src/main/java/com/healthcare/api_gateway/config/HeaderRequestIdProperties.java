package com.healthcare.api_gateway.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "gateway.request-id.header")
public record HeaderRequestIdProperties(
        @NotBlank String name
) {
}
