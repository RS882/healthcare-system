package com.healthcare.aiservice.security.properties;


import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.user-context")
public record UserContextVerifyProperties(

        @NotBlank(message = "User context issuer must not be blank.")
        String issuer,

        @NotBlank(message = "User context key ID must not be blank.")
        String keyId,

        String publicKeyPath,

        String publicKeyPem
) {}
