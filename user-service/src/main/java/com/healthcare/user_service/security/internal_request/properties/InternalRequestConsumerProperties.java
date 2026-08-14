package com.healthcare.user_service.security.internal_request.properties;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Set;

@Validated
@ConfigurationProperties(
        prefix = "internal-request"
)
public record InternalRequestConsumerProperties(

        @NotBlank(message = "Internal request grant key prefix must not be null.")
        String keyPrefix,

        @NotBlank(message ="Header internal request name must not be null.")
        String headerName,

        @NotBlank(message ="Current service name must not be null.")
        String serviceName,

        @NotEmpty(message ="Allowed issuers set must not be empty.")
        Set<String> allowedIssuers
) {
}
