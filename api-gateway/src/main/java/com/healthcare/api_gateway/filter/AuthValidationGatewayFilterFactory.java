package com.healthcare.api_gateway.filter;

import com.healthcare.api_gateway.config.properties.AuthValidationProperties;
import com.healthcare.api_gateway.config.properties.HeaderRequestIdProperties;
import com.healthcare.api_gateway.utilite.ExchangeAttrs;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

import static com.healthcare.api_gateway.filter.constant.AttrKeys.USER_ID_ATTR_KEY;
import static com.healthcare.api_gateway.filter.constant.AttrKeys.USER_ROLES_ATTR_KEY;

/**
 * Calls auth-service validation endpoint. On success stores userId/userRoles in exchange attributes.
 * Does NOT add X-User-* headers (that is responsibility of Signed User Context filter).
 */
@Component
public class AuthValidationGatewayFilterFactory
        extends AbstractGatewayFilterFactory<AuthValidationGatewayFilterFactory.Config> {

    private static final int ORDER = -900;

    private final WebClient client;
    private final AuthValidationProperties authProps;
    private final HeaderRequestIdProperties requestIdHeaderProps;

    public AuthValidationGatewayFilterFactory(WebClient.Builder webClientBuilder,
                                              AuthValidationProperties authProps,
                                              HeaderRequestIdProperties requestIdHeaderProps) {
        super(Config.class);
        this.client = webClientBuilder.build();
        this.authProps = authProps;
        this.requestIdHeaderProps = requestIdHeaderProps;
    }

    @Getter
    @Setter
    public static class Config {
        /**
         * e.g. "/api/v1/auth/validate"
         */
        private String validatePath;

        /**
         * GET or POST (default GET)
         */
        private HttpMethod method;

        /**
         * Headers to forward to auth-service.
         * Default: Authorization + RequestId header name.
         */
        private List<String> forwardHeaders;

        /**
         * e.g. "<a href="http://auth-service:8081">...</a>" (or via LB if you prefer)
         */
        private String authServiceUri;
    }

    @Override
    public GatewayFilter apply(Config config) {

        // defaults (do not assume user filled config)
        String authServiceUri = (config.getAuthServiceUri() == null || config.getAuthServiceUri().isBlank())
                ? authProps.authServiceUri()
                : config.getAuthServiceUri();

        String validatePath = (config.getValidatePath() == null || config.getValidatePath().isBlank())
                ? authProps.validatePath()
                : config.getValidatePath();

        HttpMethod method = (config.getMethod() == null) ? HttpMethod.GET : config.getMethod();

        Set<String> allowedLower = normalizeAllowedHeaders(
                (config.getForwardHeaders() == null || config.getForwardHeaders().isEmpty())
                        ? List.of(HttpHeaders.AUTHORIZATION, requestIdHeaderProps.name())
                        : config.getForwardHeaders()
        );

        final String fullUri = joinUri(authServiceUri, validatePath);

        GatewayFilter filter = (exchange, chain) ->
                callAuth(exchange, fullUri, method, allowedLower)
                        .flatMap(ctx -> {
                            // store structured data (no CSV)
                            ExchangeAttrs.put(exchange, USER_ID_ATTR_KEY, ctx.userId());
                            ExchangeAttrs.put(exchange, USER_ROLES_ATTR_KEY, ctx.userRoles());
                            return chain.filter(exchange);
                        });
        // NOTE: if callAuth() completed the response (401/403/other error),
        // it returns Mono.empty() and chain won't be executed.

        return new OrderedGatewayFilter(filter, ORDER);
    }

    private Mono<AuthContext> callAuth(ServerWebExchange exchange,
                                       String fullUri,
                                       HttpMethod method,
                                       Set<String> allowedLower) {

        HttpHeaders incoming = exchange.getRequest().getHeaders();

        WebClient.RequestHeadersSpec<?> spec =
                (method == HttpMethod.POST)
                        ? client.post().uri(fullUri)
                        : client.get().uri(fullUri);

        return spec
                .accept(MediaType.APPLICATION_JSON)
                .headers(out -> copySelectedHeaders(incoming, out, allowedLower))
                .exchangeToMono(resp -> {
                    if (resp.statusCode().isError()) {
                        // propagate status to client and stop filter chain
                        exchange.getResponse().setStatusCode(resp.statusCode());
                        return exchange.getResponse().setComplete().then(Mono.empty());
                    }
                    return resp.bodyToMono(AuthContext.class);
                });
    }

    private void copySelectedHeaders(HttpHeaders from, HttpHeaders to, Set<String> allowedLower) {
        if (allowedLower == null || allowedLower.isEmpty()) {
            // Not recommended, but keep safe behavior:
            // do NOT blindly forward all headers by default.
            return;
        }

        for (Map.Entry<String, List<String>> e : from.entrySet()) {
            String name = e.getKey();
            if (name == null) continue;

            if (allowedLower.contains(name.toLowerCase(Locale.ROOT))) {
                // copy values (defensive copy)
                to.put(name, List.copyOf(e.getValue()));
            }
        }
    }

    private static Set<String> normalizeAllowedHeaders(List<String> headers) {
        // LinkedHashSet keeps deterministic order (useful in tests/debug)
        return headers.stream()
                .filter(h -> h != null && !h.isBlank())
                .map(h -> h.toLowerCase(Locale.ROOT))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static String joinUri(String base, String path) {
        String b = (base == null) ? "" : base.trim();
        String p = (path == null) ? "" : path.trim();

        if (b.endsWith("/") && p.startsWith("/")) return b.substring(0, b.length() - 1) + p;
        if (!b.endsWith("/") && !p.startsWith("/")) return b + "/" + p;
        return b + p;
    }

    /**
     * Expected response from auth-service validate endpoint.
     * Replace with your actual DTO/record if you already have one.
     */
    public record AuthContext(Long userId, List<String> userRoles) {
    }
}
