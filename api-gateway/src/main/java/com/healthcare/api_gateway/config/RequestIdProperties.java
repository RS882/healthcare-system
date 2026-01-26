package com.healthcare.api_gateway.config;


import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "request-id")
public record RequestIdProperties(
        String prefix,
        Duration ttl,
        String value
) {}

