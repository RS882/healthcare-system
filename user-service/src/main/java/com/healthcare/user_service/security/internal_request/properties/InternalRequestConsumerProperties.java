package com.healthcare.user_service.security.internal_request.properties;


import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(
        prefix = "internal-request"
)
public record InternalRequestConsumerProperties(

        @NotBlank
        String keyPrefix,

        @NotBlank
        String headerName
) {
}
