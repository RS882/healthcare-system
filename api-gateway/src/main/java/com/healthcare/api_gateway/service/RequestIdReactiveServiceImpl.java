package com.healthcare.api_gateway.service;

import com.healthcare.api_gateway.config.RequestIdProperties;
import com.healthcare.api_gateway.service.interfaces.RequestIdReactiveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class RequestIdReactiveServiceImpl implements RequestIdReactiveService {

    private final ReactiveStringRedisTemplate redis;
    private final RequestIdProperties props;

    @Override
    public boolean isValidUuid(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    @Override
    public String resolveOrGenerate(String headerValue) {
        return Optional.ofNullable(headerValue)
                .filter(v -> !v.isBlank())
                .filter(this::isValidUuid)
                .orElseGet(() -> UUID.randomUUID().toString());
    }

    @Override
    public Mono<Boolean> save(String requestId) {
        if (!isValidUuid(requestId)) {
            return Mono.just(false);
        }

        String redisKey = toRedisKey(requestId);

        return redis.opsForValue()
                .setIfAbsent(redisKey, props.value(), props.ttl())
                .doOnError(ex -> log.warn("Failed to save requestId {} to Redis", requestId, ex))
                .onErrorReturn(false);
    }

    @Override
    public Mono<Boolean> exists(String requestId) {
        if (!isValidUuid(requestId)) {
            return Mono.just(false);
        }
        return redis.hasKey(toRedisKey(requestId))
                .onErrorReturn(false);
    }

    @Override
    public Mono<Duration> ttl(String requestId) {
        if (!isValidUuid(requestId)) {
            return Mono.just(Duration.ZERO);
        }
        return redis.getExpire(toRedisKey(requestId))
                .map(d -> d == null || d.isNegative() ? Duration.ZERO : d)
                .onErrorReturn(Duration.ZERO);
    }

    @Override
    public String toRedisKey(String requestId) {
        return props.prefix() + requestId;
    }
}
