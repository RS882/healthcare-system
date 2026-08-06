package com.healthcare.auth_service.service.internal_request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.auth_service.exception_handler.exception.InternalRequestGrantCreationException;
import com.healthcare.auth_service.exception_handler.exception.InternalRequestGrantUnavailableException;
import com.healthcare.auth_service.service.internal_request.dto.InternalRequestGrant;
import com.healthcare.auth_service.service.internal_request.interfaces.InternalRequestGrantIssuer;
import com.healthcare.auth_service.service.internal_request.interfaces.InternalRequestIdGenerator;
import com.healthcare.auth_service.service.internal_request.properties.InternalRequestIssuerProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RedisInternalRequestGrantIssuer
        implements InternalRequestGrantIssuer {

    private static final int MAX_GENERATION_ATTEMPTS = 5;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final InternalRequestIssuerProperties props;
    private final InternalRequestIdGenerator requestIdGenerator;

    @Override
    public UUID issue(
            String targetService,
            HttpMethod method,
            String path
    ) {
        validateArguments(
                targetService,
                method,
                path
        );

        InternalRequestGrant grant =
                InternalRequestGrant.builder()
                        .issuer(props.issuer())
                        .target(targetService.strip())
                        .method(method.name())
                        .path(normalizePath(path))
                        .createdAt(Instant.now())
                        .build();

        String serializedGrant =
                serialize(grant);

        try {
            for (int attempt = 1;
                 attempt <= MAX_GENERATION_ATTEMPTS;
                 attempt++) {

                UUID internalRequestId =
                        requestIdGenerator.generate();

                Boolean saved =
                        redisTemplate.opsForValue()
                                .setIfAbsent(
                                        toRedisKey(internalRequestId),
                                        serializedGrant,
                                        props.ttl()
                                );

                if (Boolean.TRUE.equals(saved)) {
                    return internalRequestId;
                }
            }
        } catch (DataAccessException exception) {
            throw new InternalRequestGrantUnavailableException(
                    "Failed to store internal request grant",
                    exception
            );
        }

        throw new InternalRequestGrantUnavailableException(
                "Failed to generate and store a unique internal request grant"
        );
    }

    private void validateArguments(
            String targetService,
            HttpMethod method,
            String path
    ) {
        if (!StringUtils.hasText(targetService)) {
            throw new IllegalArgumentException(
                    "Target service must not be blank"
            );
        }

        if (method == null) {
            throw new IllegalArgumentException(
                    "HTTP method must not be null"
            );
        }

        if (!StringUtils.hasText(path)) {
            throw new IllegalArgumentException(
                    "Request path must not be blank"
            );
        }
    }

    private String normalizePath(
            String path
    ) {
        String normalized =
                path.strip();

        int queryIndex =
                normalized.indexOf('?');

        if (queryIndex >= 0) {
            normalized =
                    normalized.substring(
                            0,
                            queryIndex
                    );
        }

        return normalized.startsWith("/")
                ? normalized
                : "/" + normalized;
    }

    private String serialize(
            InternalRequestGrant grant
    ) {
        try {
            return objectMapper.writeValueAsString(
                    grant
            );
        } catch (JsonProcessingException exception) {
            throw new InternalRequestGrantCreationException(
                    "Failed to serialize internal request grant",
                    exception
            );
        }
    }

    private String toRedisKey(
            UUID requestId
    ) {
        return props.keyPrefix() + requestId;
    }
}