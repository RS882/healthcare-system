package com.healthcare.auth_service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
        info = @Info(
                title = "Healthcare system: authorisation service",
                description = "API for operations for registration, authorisation of user",
                version = "1.0.0"
        )
)
public class SwaggerConfig {
}
