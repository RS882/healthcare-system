package com.healthcare.api_gateway.filter;

import com.healthcare.api_gateway.config.properties.UserContextProperties;
import com.healthcare.api_gateway.filter.signing.UserContextSigner;
import com.healthcare.api_gateway.utilite.ExchangeAttrs;
import com.healthcare.api_gateway.utilite.GatewaySecurityHeaders;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static com.healthcare.api_gateway.filter.constant.AttrKeys.*;
import static com.healthcare.api_gateway.filter.constant.ProtectedPrefixes.PROTECTED_PREFIXES;

/**
 * Adds signed X-User-Context (RSA JWS) to downstream requests.
 * Must be used only on secured routes (after AuthValidation filter).
 */
@Component
public class AddSignedUserContextGatewayFilterFactory
        extends AbstractGatewayFilterFactory<AddSignedUserContextGatewayFilterFactory.Config> {

    private static final int ORDER = -800;

    private final UserContextProperties contextProps;
    private final UserContextSigner signer;

    public AddSignedUserContextGatewayFilterFactory(UserContextProperties contextProps,
                                                    UserContextSigner signer) {
        super(Config.class);
        this.contextProps = contextProps;
        this.signer = signer;
    }

    @Getter
    @Setter
    public static class Config {
        /**
         * Header name for the signed user context. Default from properties (e.g. X-User-Context)
         */
        private String userContextHeader;

        /**
         * Token TTL for exp claim. Default from properties.
         * If set to null or <= 0 -> properties value.
         */
        private Duration ttl;

        /**
         * If true: missing attrs => 401 (fail-closed). Default: true.
         * If false: missing attrs => pass-through (fail-open).
         */
        private Boolean failClosed;
    }

    @Override
    public GatewayFilter apply(Config config) {

        final String ctxHeader = (config.getUserContextHeader() == null || config.getUserContextHeader().isBlank())
                ? contextProps.userContextHeader()
                : config.getUserContextHeader();

        final Duration ttl = normalizeTtl(config.getTtl(), contextProps.ttl());

        final boolean failClosed = config.getFailClosed() == null || config.getFailClosed();

        GatewayFilter delegate = (exchange, chain) -> {
            Optional<UserAttrs> attrsOpt = readRequiredAttrs(exchange);

            if (attrsOpt.isEmpty()) {
                if (!failClosed) {
                    return chain.filter(exchange);
                }
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            UserAttrs attrs = attrsOpt.get();

            String signed = signer.sign(attrs.userId(), attrs.roles(), attrs.requestId(), ttl);

            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .headers(h -> {
                        GatewaySecurityHeaders.removeByPrefixes(h, PROTECTED_PREFIXES);
                        GatewaySecurityHeaders.setTrusted(h, ctxHeader, signed);
                    })
                    .build();

            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(mutatedRequest)
                    .build();

            return chain.filter(mutatedExchange);
        };

        return new OrderedGatewayFilter(delegate, ORDER);
    }

    private static Duration normalizeTtl(Duration configured, Duration fallback) {
        Duration base = (fallback == null) ? Duration.ofSeconds(30) : fallback;

        if (configured == null) {
            return base;
        }

        if (configured.isZero() || configured.isNegative()) {
            return base;
        }
        return configured;
    }

    private static Optional<UserAttrs> readRequiredAttrs(ServerWebExchange exchange) {

        String requestId = ExchangeAttrs.get(exchange, REQUEST_ID_ATTR_KEY, String.class)
                .orElse(null);
        Long userId = ExchangeAttrs.get(exchange, USER_ID_ATTR_KEY, Long.class)
                .orElse(null);
        List<String> roles = ExchangeAttrs.getUnchecked(exchange, USER_ROLES_ATTR_KEY)
                .orElse(List.of());

        if (userId == null) {
            return Optional.empty();
        }
        if (requestId == null || requestId.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(
                UserAttrs.get(
                        String.valueOf(userId),
                        roles,
                        requestId
                )
        );
    }

    private record UserAttrs(
                             String userId,
                             List<String> roles,
                             String requestId) {
        static UserAttrs get(String userId, List<String> roles, String requestId) {
            return new UserAttrs( userId, List.copyOf(roles), requestId);
        }
    }
}

