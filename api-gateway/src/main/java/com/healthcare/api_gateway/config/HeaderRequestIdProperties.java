package com.healthcare.api_gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "header-request-id")
public record HeaderRequestIdProperties(
        String name
) {
}
