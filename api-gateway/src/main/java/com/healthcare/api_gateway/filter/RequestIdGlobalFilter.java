package com.healthcare.api_gateway.filter;

import com.healthcare.api_gateway.config.properties.HeaderRequestIdProperties;
import com.healthcare.api_gateway.service.interfaces.RequestIdReactiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static com.healthcare.api_gateway.filter.constant.AttrRequestId.ATTR_REQUEST_ID;
import static com.healthcare.api_gateway.filter.constant.RequestIdContextKeys.REQUEST_ID;

@Component
@RequiredArgsConstructor
public class RequestIdGlobalFilter implements GlobalFilter, Ordered {

    private static final int ORDER = -1000;

    private final RequestIdReactiveService requestIdService;
    private final HeaderRequestIdProperties props;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             GatewayFilterChain chain) {

        String headerValue = exchange.getRequest().getHeaders().getFirst(props.name());

        String requestId = requestIdService.resolveOrGenerate(headerValue);

        ServerWebExchange mutatedExchange = mutateExchangeRequest(exchange, requestId);

        return requestIdService.save(requestId)
                .onErrorResume(e -> Mono.empty())
                .then(chain.filter(mutatedExchange))
                .contextWrite(ctx -> ctx.put(REQUEST_ID, requestId));
    }

    private ServerWebExchange mutateExchangeRequest(ServerWebExchange exchange, String requestId) {

        ServerHttpRequest mutatedRequest = mutateRequestHeaders(exchange, requestId);

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        mutatedExchange.getAttributes().put(ATTR_REQUEST_ID, requestId);

        return mutatedExchange;
    }

    private ServerHttpRequest mutateRequestHeaders(ServerWebExchange exchange, String requestId) {

        Map<String, String> headers = new HashMap<>();
        headers.put(props.name(), requestId);

        return mutateRequestHeaders(exchange, headers);
    }

    private ServerHttpRequest mutateRequestHeaders(ServerWebExchange exchange, Map<String, String> headers) {

        if (headers == null || headers.isEmpty()) {
            return exchange.getRequest();
        }

        return exchange.getRequest()
                .mutate()
                .headers(httpHeaders -> {
                    headers.forEach((key, value) -> {
                        if (key != null && !key.isBlank() && value != null && !value.isBlank()) {
                            httpHeaders.remove(key);
                            httpHeaders.add(key, value);
                        }
                    });
                })
                .build();
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}

