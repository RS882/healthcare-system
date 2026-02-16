package com.healthcare.api_gateway.utilite;


import org.springframework.web.server.ServerWebExchange;

import java.util.Optional;

public final class ExchangeAttrs {

    private ExchangeAttrs() {}

    public static <T> void put(ServerWebExchange exchange, AttrKey<T> key, T value) {
        if (exchange == null || key == null) return;
        exchange.getAttributes().put(key.name(), value);
    }

    public static <T> Optional<T> get(ServerWebExchange exchange, AttrKey<T> key, Class<T> type) {
        if (exchange == null || key == null || type == null) return Optional.empty();

        Object v = exchange.getAttributes().get(key.name());
        if (v == null) return Optional.empty();
        if (!type.isInstance(v)) return Optional.empty();

        return Optional.of(type.cast(v));
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getUnchecked(ServerWebExchange exchange, AttrKey<T> key) {
        if (exchange == null || key == null) return Optional.empty();
        Object v = exchange.getAttributes().get(key.name());
        return (v == null) ? Optional.empty() : Optional.of((T) v);
    }
}

