package com.healthcare.api_gateway.filter;

import com.healthcare.api_gateway.config.properties.AuthValidationProperties;
import com.healthcare.api_gateway.config.properties.HeaderRequestIdProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class AuthValidationGatewayFilterFactory
        extends AbstractGatewayFilterFactory<AuthValidationGatewayFilterFactory.Config> {

    private final WebClient.Builder webClientBuilder;

    private final AuthValidationProperties authValidationProps;

    private final HeaderRequestIdProperties headerRequestIdProps;

    public AuthValidationGatewayFilterFactory(WebClient.Builder webClientBuilder,
                                              AuthValidationProperties authValidationProps,
                                              HeaderRequestIdProperties headerRequestIdProps) {
        super(Config.class);
        this.webClientBuilder = webClientBuilder;
        this.authValidationProps = authValidationProps;
        this.headerRequestIdProps = headerRequestIdProps;
    }

    @Getter
    @Setter
    public static class Config {
        private String validatePath;
        private HttpMethod method;
        private List<String> forwardHeaders;
        private String userIdHeader;
        private String rolesHeader;
        private String authServiceUri;
    }

    @Override
    public GatewayFilter apply(Config config) {

        if (config.getAuthServiceUri() == null) config.setAuthServiceUri(authValidationProps.authServiceUri());
        if (config.getValidatePath() == null) config.setValidatePath(authValidationProps.validatePath());
        if (config.getMethod() == null) config.setMethod(HttpMethod.GET);

        if (config.getForwardHeaders() == null || config.getForwardHeaders().isEmpty()) {
            config.setForwardHeaders(List.of(HttpHeaders.AUTHORIZATION, headerRequestIdProps.name()));
        }
        config.setForwardHeaders(config.getForwardHeaders().stream()
                .map(h -> h.toLowerCase(Locale.ROOT))
                .toList());

        if (config.getUserIdHeader() == null) config.setUserIdHeader(authValidationProps.userIdHeader());
        if (config.getRolesHeader() == null) config.setRolesHeader(authValidationProps.rolesHeader());

        return (exchange, chain) -> callAuth(exchange, config)
                .flatMap(ctx -> {
                    if (ctx == null) {
                        return Mono.empty();
                    }

                    String rolesCsv = (ctx.roles() == null) ? "" : String.join(",", ctx.roles());

                    var mutatedRequest = exchange.getRequest().mutate()
                            .header(config.getUserIdHeader(), String.valueOf(ctx.userId()))
                            .header(config.getRolesHeader(), rolesCsv)
                            .build();

                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                });
    }

    private Mono<AuthContext> callAuth(ServerWebExchange exchange, Config config) {
        HttpHeaders incoming = exchange.getRequest().getHeaders();
        WebClient client = webClientBuilder.build();

        String fullAuthValidationUri = config.getAuthServiceUri() + config.getValidatePath();

        WebClient.RequestHeadersSpec<?> spec =
                (config.getMethod() == HttpMethod.POST)
                        ? client.post().uri(fullAuthValidationUri)
                        : client.get().uri(fullAuthValidationUri);

        return spec
                .accept(MediaType.APPLICATION_JSON)
                .headers(out -> copySelectedHeaders(incoming, out, config.getForwardHeaders()))
                .exchangeToMono(resp -> {
                    int code = resp.statusCode().value();

                    if (code == 401 || code == 403) {
                        exchange.getResponse().setStatusCode(resp.statusCode());
                        return exchange.getResponse().setComplete().then(Mono.empty());
                    }

                    if (code >= 400) {
                        exchange.getResponse().setStatusCode(resp.statusCode());
                        return exchange.getResponse().setComplete().then(Mono.empty());
                    }

                    return resp.bodyToMono(AuthContext.class);
                });
    }

    private void copySelectedHeaders(HttpHeaders from, HttpHeaders to, List<String> allowedLowercase) {
        if (allowedLowercase == null || allowedLowercase.isEmpty()) {
            for (Map.Entry<String, List<String>> e : from.entrySet()) {
                to.put(e.getKey(), e.getValue());
            }
            return;
        }

        for (Map.Entry<String, List<String>> e : from.entrySet()) {
            String name = e.getKey();
            if (allowedLowercase.contains(name.toLowerCase(Locale.ROOT))) {
                to.put(name, e.getValue());
            }
        }
    }
}

