package com.healthcare.aiservice.security.internal_request.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(
        prefix = "clients.user-service.internal"
)
public record UserServiceInternalClientProperties(

        @NotBlank(message = "Target service must not be null.")
        String targetService,

        @NotBlank(message = "Base path must not be null.")
        String basePath

) {
}