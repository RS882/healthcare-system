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
        @NotNull(message = "Active prompt must not be null.")
        ActivePromptProperties activePrompt,

        @Valid
        @NotNull(message = "User auth info must not be null.")
        UserAuthInfoCacheProperties userAuthInfo,

        @NotNull(message = "Cache default ttl must not be null.")
        @DurationMin(
                seconds = 1,
                message = "Cache default ttl must be at least 1s.")
        Duration defaultTtl
) {
}

