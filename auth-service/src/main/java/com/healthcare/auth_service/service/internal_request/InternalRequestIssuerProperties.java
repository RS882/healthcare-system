package com.healthcare.auth_service.service.internal_request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "internal-request")
public record InternalRequestIssuerProperties(

        @NotBlank(message ="Internal request grant issuer must not be null.")
        String issuer,

        @NotBlank(message = "Internal request grant key prefix must not be null.")
        String keyPrefix,

        @NotNull(message = "Internal request grant ttl must not be null.")
        @DurationMin(
                millis = 1,
                message = "Internal request grant ttl must be at least 1 ms"
        )
        Duration ttl
) {
}