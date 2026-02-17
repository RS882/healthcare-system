package com.healthcare.api_gateway.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

public final class SecurityGuard {

    private SecurityGuard() {
    }

    /**
     * Fail-closed guard.
     *
     * If value present -> Mono.just(value)
     * If missing -> writes 401 and completes response
     */
    public static <T> Mono<T> require(ServerWebExchange exchange,
                                      Optional<T> optional) {

        if (optional.isPresent()) {
            return Mono.just(optional.get());
        }

        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete().then(Mono.empty());
    }
}

