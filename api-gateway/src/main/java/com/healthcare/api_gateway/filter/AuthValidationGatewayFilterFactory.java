package com.healthcare.api_gateway.filter;

import com.healthcare.api_gateway.config.properties.HeaderRequestIdProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class AuthValidationGatewayFilterFactory
        extends AbstractGatewayFilterFactory<AuthValidationGatewayFilterFactory.Config> {

    private final WebClient.Builder webClientBuilder;
    private final HeaderRequestIdProperties headerRequestIdProperties;

    @Getter
    @Setter
    public static class Config {
        /**
         * Путь на auth-service (без хоста), например: /api/v1/auth/validation
         */
        private String validatePath = "/api/v1/auth/validation";

        /**
         * Метод вызова auth-service: GET или POST
         */
        private HttpMethod method = HttpMethod.GET;

        /**
         * Какие headers прокинуть в auth-service.
         * Рекомендуется минимум: authorization (+ x-request-id если нужно).
         */
        private List<String> forwardHeaders = Stream.of(
                HttpHeaders.AUTHORIZATION,
                        "x-request-id")
                .map(h -> h.toLowerCase(Locale.ROOT))
                .toList();

        /**
         * Куда писать результат
         */
        private String userIdHeader = "X-User-Id";
        private String rolesHeader = "X-User-Roles";
    }

    @Override
    public GatewayFilter apply(Config config) {

        System.out.println();
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

        WebClient.RequestHeadersSpec<?> spec =
                (config.getMethod() == HttpMethod.POST)
                        ? client.post().uri("lb://auth-service" + config.getValidatePath())
                        : client.get().uri("lb://auth-service" + config.getValidatePath());

        return spec
                .accept(MediaType.APPLICATION_JSON)
                .headers(out -> copySelectedHeaders(incoming, out, config.getForwardHeaders()))
                // body не отправляем
                .retrieve()
                .onStatus(status -> status == HttpStatus.UNAUTHORIZED || status == HttpStatus.FORBIDDEN,
                        resp ->
                                Mono.error(new AuthDeniedException(
                                        HttpStatus.valueOf(resp.statusCode().value()))))
                .bodyToMono(AuthContext.class)
                .onErrorResume(AuthDeniedException.class, ex -> writeStatus(exchange, ex.status));
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

    private Mono<AuthContext> writeStatus(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete().thenReturn(null);
    }

    private static class AuthDeniedException extends RuntimeException {
        private final HttpStatus status;

        private AuthDeniedException(HttpStatus status) {
            this.status = status;
        }
    }
}

