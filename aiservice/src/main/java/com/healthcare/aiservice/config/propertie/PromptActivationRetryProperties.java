package com.healthcare.aiservice.config.propertie;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(
        prefix = "ai.prompt.activation-retry"
)
public record PromptActivationRetryProperties(

        @Min(
                value = 1,
                message = "Prompt activation retry max attempts must be at least 1"
        )
        int maxAttempts,

        @NotNull(message = "Prompt activation retry initial delay must not be null.")
        @DurationMin(
                millis = 1,
                message = "Prompt activation retry initial delay must be at least 1 ms"
        )
        Duration initialDelay,

        @DecimalMin(
                value = "1.0",
                message = "Prompt activation retry multiplier must be at least 1.0"
        )
        double multiplier,

        @NotNull(message = "Prompt activation retry max delay must not be null.")
        @DurationMin(
                millis = 1,
                message = "Prompt activation retry max delay must be at least 1 ms"
        )
        Duration maxDelay
) {
}