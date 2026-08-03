package com.healthcare.aiservice.config.propertie.cache_propertie;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.time.DurationMin;

import java.time.Duration;

public record ActivePromptProperties(

        @NotNull(message = "Active prompt cache ttl must not be null.")
        @DurationMin(
                seconds = 1,
                message = "Active prompt cache ttl must be at least 1s.")
        Duration ttl
) {
}
