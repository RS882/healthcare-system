package com.healthcare.api_gateway.config.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "security.user-context")
public record UserContextSigningProperties(
        @NotBlank String issuer,
        @NotBlank String keyId,
        @NotBlank    String privateKeyPem,
        @NotBlank  String privateKeyPath,
        @NotNull
        @DurationMin(seconds = 1)
        Duration defaultTtl
) {
}
