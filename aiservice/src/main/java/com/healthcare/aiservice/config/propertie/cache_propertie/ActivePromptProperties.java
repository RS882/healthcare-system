package com.healthcare.aiservice.config.propertie.cache_propertie;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.time.DurationMin;

import java.time.Duration;

public record ActivePromptProperties(

        @NotNull
        String cacheName,

        @NotNull
        @DurationMin(seconds = 1)
        Duration ttl
) {
}
