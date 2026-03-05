package com.healthcare.user_service.config.properties;


import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.user-context")
public record UserContextVerifyProperties(
        @NotBlank
        String issuer,
        @NotBlank
        String keyId,
        String publicKeyPath,
        String publicKeyPem
) {}
