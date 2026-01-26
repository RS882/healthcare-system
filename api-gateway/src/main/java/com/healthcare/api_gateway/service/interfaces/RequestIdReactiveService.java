package com.healthcare.api_gateway.service.interfaces;

import reactor.core.publisher.Mono;

import java.time.Duration;

public interface RequestIdReactiveService {

    boolean isValidUuid(String value);

    String resolveOrGenerate(String headerValue);

    Mono<Boolean> save(String requestId);

    Mono<Boolean> exists(String requestId);

    Mono<Duration> ttl(String requestId);

    String toRedisKey(String requestId);
}
