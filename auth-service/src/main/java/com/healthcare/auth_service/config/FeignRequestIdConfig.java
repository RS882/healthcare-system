package com.healthcare.auth_service.config;

import com.healthcare.auth_service.config.properties.HeaderRequestIdProperties;
import com.healthcare.auth_service.service.interfacies.RequestIdService;
import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FeignRequestIdConfig {

    private final HeaderRequestIdProperties props;
    private final RequestIdService requestIdService;


    @Bean
    public RequestInterceptor requestIdInterceptor() {
        return template -> {
            String requestId = requestIdService.getRequestId().toString();
            template.header(props.name(), requestId);
        };
    }
}

