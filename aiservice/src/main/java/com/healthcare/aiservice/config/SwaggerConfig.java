package com.healthcare.aiservice.config;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Healthcare System: AI Service",
                description = """
                        AI-powered healthcare microservice for:
                        
                        - medical note summarization
                        - patient message classification
                        - medical information extraction
                        - healthcare communication assistance
                        """,
                version = "1.0.0"
        )
)
public class SwaggerConfig {
}
