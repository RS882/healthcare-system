package com.healthcare.api_gateway.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RequestIdGlobalFilter implements GlobalFilter, Ordered {

    public static final String HEADER_REQUEST_ID = "X-Request-Id";

    private static final String REDIS_KEY_PREFIX = "request-id:";
    private static final Duration TTL = Duration.ofSeconds(30);
    private static final String REDIS_VALUE = "gateway";

    private final ReactiveStringRedisTemplate redis;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             GatewayFilterChain chain) {

        String requestId = resolveOrGenerateRequestId(exchange.getRequest());

        String redisKey = REDIS_KEY_PREFIX + requestId;

        Mono<Boolean> saveMono = redis.opsForValue()
                .setIfAbsent(redisKey, REDIS_VALUE, TTL)
                .onErrorResume(ex -> Mono.just(false));

        return saveMono
                .flatMap(saved -> {
                    ServerHttpRequest mutatedRequest = exchange.getRequest()
                            .mutate()
                            .header(HEADER_REQUEST_ID, requestId)
                            .build();

                    ServerWebExchange mutatedExchange = exchange.mutate()
                            .request(mutatedRequest)
                            .build();

                    mutatedExchange.getAttributes().put(HEADER_REQUEST_ID, requestId);

                    return chain.filter(mutatedExchange);
                });
    }

    private String resolveOrGenerateRequestId(ServerHttpRequest request) {
        return Optional.ofNullable(request.getHeaders().getFirst(HEADER_REQUEST_ID))
                .filter(v -> !v.isBlank())
                .orElseGet(() -> UUID.randomUUID().toString());
    }

    @Override
    public int getOrder() {
        return -1000;
    }
}
