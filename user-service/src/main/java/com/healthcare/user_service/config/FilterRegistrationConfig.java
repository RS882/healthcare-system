package com.healthcare.user_service.config;

import com.healthcare.user_service.security.internal_request.filter.InternalRequestAuthenticationFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterRegistrationConfig {

    @Bean
    @ConditionalOnProperty(
            name = "internal-request-filter.enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public FilterRegistrationBean<InternalRequestAuthenticationFilter>
    internalRequestAuthenticationFilterRegistration(
            InternalRequestAuthenticationFilter filter
    ) {

        FilterRegistrationBean<InternalRequestAuthenticationFilter> registration =
                new FilterRegistrationBean<>(filter);

        registration.setEnabled(false);

        return registration;
    }
}