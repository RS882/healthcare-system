package com.healthcare.user_service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
        info = @Info(
                title = "Healthcare system: user service",
                description = "API for operations for registration and CRUD operation of user",
                version = "1.0.0"
        )
)
public class SwaggerConfig {
}
