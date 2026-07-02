package com.healthcare.aiservice.config;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Healthcare System: AI Service",
                description = """
                        AI-powered healthcare microservice providing structured medical summarization,
                        medical information extraction, patient message classification,
                        AI audit logging, monitoring and usage statistics.
                        """,
                version = "1.0.0"
        )
)
public class SwaggerConfig {
}
