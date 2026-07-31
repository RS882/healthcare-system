package com.healthcare.aiservice.config.propertie.cache_propertie;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "cache")
public record CacheProperties(

        @Valid
        @NotNull
        ActivePromptProperties activePrompt,

        @NotNull
        @DurationMin(seconds = 1)
        Duration defaultTtl
) {
}

