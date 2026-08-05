package com.healthcare.aiservice.security.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "user-context")
public record UserContextProperties(

        @NotBlank(message = "User context header must not be blank.")
        String userContextHeader
) {
}
