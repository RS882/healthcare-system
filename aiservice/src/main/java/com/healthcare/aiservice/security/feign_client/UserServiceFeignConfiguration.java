package com.healthcare.aiservice.security.feign_client;

import com.healthcare.aiservice.security.internal_request.InternalRequestFeignInterceptor;
import com.healthcare.aiservice.security.internal_request.interfaces.InternalRequestGrantIssuer;
import com.healthcare.aiservice.security.internal_request.properties.InternalRequestIssuerProperties;
import com.healthcare.aiservice.security.internal_request.properties.UserServiceInternalClientProperties;
import com.healthcare.aiservice.security.properties.HeaderRequestIdProperties;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;

public class UserServiceFeignConfiguration {

    @Bean
    public ErrorDecoder userServiceFeignErrorDecoder() {
        return new UserServiceFeignErrorDecoder();
    }

    @Bean
    public RequestInterceptor internalRequestFeignInterceptor(
            InternalRequestGrantIssuer grantIssuer,
            InternalRequestIssuerProperties issuerProperties,
            UserServiceInternalClientProperties clientProperties,
            HeaderRequestIdProperties requestIdProperties

    ) {
        return new InternalRequestFeignInterceptor(
                grantIssuer,
                issuerProperties,
                clientProperties,
                requestIdProperties
        );
    }
}