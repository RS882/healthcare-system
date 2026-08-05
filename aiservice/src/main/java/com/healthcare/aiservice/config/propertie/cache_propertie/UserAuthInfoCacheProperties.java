package com.healthcare.aiservice.config.propertie.cache_propertie;


import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.time.DurationMin;

import java.time.Duration;

public record UserAuthInfoCacheProperties(

        @NotNull(message = "User auth info ttl must not be null.")
        @DurationMin(
                seconds = 1,
                message = "User auth info ttl must be at least 1s.")
        Duration ttl
) {
}