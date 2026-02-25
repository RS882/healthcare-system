package com.healthcare.api_gateway.filter;

import com.healthcare.api_gateway.config.properties.HeaderRequestIdProperties;
import com.healthcare.api_gateway.service.interfaces.RequestIdReactiveService;
import com.healthcare.api_gateway.utilite.ExchangeAttrs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static com.healthcare.api_gateway.filter.constant.AttrKeys.REQUEST_ID_ATTR_KEY;
import static com.healthcare.api_gateway.filter.constant.RequestIdContextKeys.REQUEST_ID_CONTEXT_KEY_NAME;
import static com.healthcare.api_gateway.utilite.GatewaySecurityHeaders.removeByNames;
import static com.healthcare.api_gateway.utilite.GatewaySecurityHeaders.setTrusted;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
        name="gateway.request-id.enabled",
        havingValue="true",
        matchIfMissing=true)
public class RequestIdGlobalFilter implements GlobalFilter, Ordered {

    private static final int ORDER = -1000;

    private final RequestIdReactiveService requestIdService;
    private final HeaderRequestIdProperties props;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             GatewayFilterChain chain) {

        String headerValue = getCheckedHeaderValue(exchange.getRequest().getHeaders(), props.name());

        String requestId = requestIdService.resolveOrGenerate(headerValue);

        ServerWebExchange mutatedExchange = mutateExchangeRequest(exchange, requestId);

        return requestIdService.save(requestId)
                .onErrorResume(e -> Mono.empty())
                .then(chain.filter(mutatedExchange))
                .contextWrite(ctx -> ctx.put(REQUEST_ID_CONTEXT_KEY_NAME, requestId));
    }

    private ServerWebExchange mutateExchangeRequest(ServerWebExchange exchange, String requestId) {

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .headers(h -> {
                    removeByNames(h, props.name());
                    setTrusted(h, props.name(), requestId);
                })
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        ExchangeAttrs.put(mutatedExchange, REQUEST_ID_ATTR_KEY, requestId);

        return mutatedExchange;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    private String getCheckedHeaderValue(HttpHeaders headers, String headerName) {
        if (headerName == null || headerName.isBlank()) return null;
        String headerValue = headers.getFirst(headerName);

        headerValue = headerValue != null ? headerValue.trim() : null;
        if (headerValue != null && headerValue.isBlank()) {
            return null;
        }
        if (headerValue != null && headerValue.length() > 128) {
            log.debug("Incoming {} header too long: {} chars. Ignored.", headerName, headerValue.length());
            return null;
        }
        return headerValue;
    }
}

