package com.healthcare.api_gateway.config.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "auth-validation")
public record AuthValidationProperties(
        @NotBlank String authServiceUri,
        @NotBlank String validatePath,
        @NotBlank String userIdHeader,
        @NotBlank String rolesHeader
) {
}
