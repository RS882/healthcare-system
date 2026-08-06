package com.healthcare.auth_service.service.feignClient.config;


import com.healthcare.auth_service.exception_handler.exception.FeignBadRequestException;
import com.healthcare.auth_service.service.internal_request.interfaces.InternalRequestGrantIssuer;
import com.healthcare.auth_service.service.internal_request.properties.InternalRequestIssuerProperties;
import com.healthcare.auth_service.service.internal_request.properties.UserServiceInternalClientProperties;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;

import java.util.UUID;

public class InternalRequestFeignConfiguration {

    @Bean
    public RequestInterceptor internalRequestIdInterceptor(
            InternalRequestGrantIssuer grantIssuer,
            InternalRequestIssuerProperties internalRequestProperties,
            UserServiceInternalClientProperties clientProperties
    ) {
        return requestTemplate -> {

            HttpMethod method = resolveHttpMethod(requestTemplate.method());

            String path = requestTemplate.path();

            if (path.isBlank()) {
                throw new FeignBadRequestException(
                        "Feign request path is missing"
                );
            }

            UUID internalRequestId = grantIssuer.issue(
                    clientProperties.targetService(),
                    method,
                    path
            );

            requestTemplate.header(
                    internalRequestProperties.headerName(),
                    internalRequestId.toString()
            );
        };
    }

    private HttpMethod resolveHttpMethod(
            String method
    ) {
        if (method == null || method.isBlank()) {
            throw new FeignBadRequestException(
                    "Feign request HTTP method is missing"
            );
        }

        try {
            return HttpMethod.valueOf(
                    method.strip().toUpperCase()
            );

        } catch (IllegalArgumentException exception) {
            throw new FeignBadRequestException(
                    "Unsupported Feign HTTP method: "
                            + method,
                    exception
            );
        }
    }
}