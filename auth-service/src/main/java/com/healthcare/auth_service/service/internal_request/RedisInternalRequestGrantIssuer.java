package com.healthcare.auth_service.service.internal_request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.healthcare.auth_service.service.internal_request.interfaces.InternalRequestGrantIssuer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RedisInternalRequestGrantIssuer implements InternalRequestGrantIssuer {

    private static final int MAX_ATTEMPTS = 5;
    private final Duration DEFAULT_TTL = Duration.ofSeconds(30);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final InternalRequestIssuerProperties props;

    @Override
    public UUID issue(
            String target,
            HttpMethod method,
            String path
    ) {
        InternalRequestGrant grant = InternalRequestGrant.builder()
                .issuer(props.issuer())
                .target(target)
                .method(method.name())
                .path(normalizePath(path))
                .createdAt(Instant.now())
                .build();

        UUID requestId = UUID.randomUUID();

        String serializedGrant = serialize(grant,  requestId);

        String redisKey =  toRedisKey(requestId);
        Duration grantTtl = normalizeTtl(props.ttl());

        for (int attempt = 1;
             attempt <= MAX_ATTEMPTS;
             attempt++) {

            Boolean created =
                    redisTemplate.opsForValue().setIfAbsent(
                            redisKey,
                            serializedGrant,
                            grantTtl);

            if (Boolean.TRUE.equals(created)) {
                return requestId;
            }
        }

        throw new IllegalStateException(
                String.format("Failed to issue internal request grant. Request ID : <%s>", requestId)
        );
    }

    private String serialize(
            InternalRequestGrant grant,
            UUID requestId
    ) {
        try {
            return objectMapper.writeValueAsString(grant);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    String.format("Failed to serialize internal request grant. Request ID : <%s>", requestId),
                    exception
            );
        }
    }

    private Duration normalizeTtl(Duration ttl) {
        return Optional.ofNullable(props.ttl())
                .filter(d -> !d.isNegative() && !d.isZero())
                .orElse(DEFAULT_TTL);
    }

    private String normalizePath(
            String path
    ) {
        if (!StringUtils.hasText(path)) {
            throw new IllegalArgumentException(
                    "Internal request path must not be blank"
            );
        }

        String normalized = path.strip();

        int queryIndex = normalized.indexOf('?');

        return queryIndex >= 0
                ? normalized.substring(0, queryIndex)
                : normalized;
    }

    private String toRedisKey(UUID requestId) {
        return props.keyPrefix() + requestId;
    }
}
