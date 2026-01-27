package com.healthcare.auth_service.config.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        @NotBlank
        String accessSecret,

        @NotBlank
        String refreshSecret,

        @NotNull
        @DurationMin(seconds = 1)
        Duration accessTokenExpiration,

        @NotNull
        @DurationMin(seconds = 1)
        Duration refreshTokenExpiration,

        @NotBlank
        String cookiePath,

        @Positive
        int maxTokens
) {}