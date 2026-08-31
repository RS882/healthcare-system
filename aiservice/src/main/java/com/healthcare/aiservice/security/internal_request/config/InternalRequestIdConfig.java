package com.healthcare.aiservice.security.internal_request.config;



import com.healthcare.aiservice.security.internal_request.interfaces.InternalRequestIdGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class InternalRequestIdConfig {

    @Bean
    public InternalRequestIdGenerator internalRequestIdGenerator() {
        return UUID::randomUUID;
    }
}
