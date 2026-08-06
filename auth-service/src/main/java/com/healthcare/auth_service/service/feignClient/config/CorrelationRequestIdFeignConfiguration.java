package com.healthcare.auth_service.service.feignClient.config;


import com.healthcare.auth_service.config.properties.HeaderRequestIdProperties;
import com.healthcare.auth_service.filter.context.RequestContextReader;
import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;

public class CorrelationRequestIdFeignConfiguration {

    @Bean
    public RequestInterceptor correlationRequestIdInterceptor(
            ObjectProvider<HttpServletRequest> requestProvider,
            HeaderRequestIdProperties properties
    ) {
        return requestTemplate -> {
            HttpServletRequest request =
                    requestProvider.getIfAvailable();

            if (request == null) {
                return;
            }

            RequestContextReader
                    .getRequestId(request)
                    .ifPresent(requestId ->
                            requestTemplate.header(
                                    properties.name(),
                                    requestId.toString()
                            )
                    );
        };
    }
}
