package com.healthcare.api_gateway.filter;

import com.healthcare.api_gateway.config.properties.HeaderRequestIdProperties;
import com.healthcare.api_gateway.service.interfaces.RequestIdReactiveService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.healthcare.api_gateway.filter.constant.AttrRequestId.ATTR_REQUEST_ID;
import static com.healthcare.api_gateway.filter.constant.RequestIdContextKeys.REQUEST_ID;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = RequestIdGlobalFilterIT.TestApp.class
)
@TestPropertySource(properties = {
        "gateway.request-id.header.name=X-Request-Id"
})
@ActiveProfiles("test")
@AutoConfigureMockMvc
@DisplayName("Request ID global filter integration tests: ")
@DisplayNameGeneration(value = DisplayNameGenerator.ReplaceUnderscores.class)
class RequestIdGlobalFilterIT {

    @Autowired
    WebTestClient webTestClient;

    @Test
    void when_Header_Missing_should_Generate_And_Propagate_To_HeaderAttrAndContext() {
        webTestClient.get()
                .uri("/test")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.header").isEqualTo("generated-id")
                .jsonPath("$.attr").isEqualTo("generated-id")
                .jsonPath("$.ctx").isEqualTo("generated-id");
    }

    @Test
    void when_Header_Present_should_Reuse_And_Propagate_To_HeaderAttrAndContext() {
        webTestClient.get()
                .uri("/test")
                .header("X-Request-Id", "client-id")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.header").isEqualTo("client-id")
                .jsonPath("$.attr").isEqualTo("client-id")
                .jsonPath("$.ctx").isEqualTo("client-id");
    }

    @SpringBootApplication
    @EnableConfigurationProperties(HeaderRequestIdProperties.class)
    static class TestApp {

        @Bean
        RouteLocator testRoutes(RouteLocatorBuilder builder) {
            return builder.routes()
                    .route("test-route", r -> r.path("/test")
                            .uri("forward:/echo"))
                    .build();
        }

        @Bean
        RequestIdReactiveService requestIdReactiveService() {
            return new RequestIdReactiveService() {
                @Override
                public boolean isValidUuid(String value) {
                    return false;
                }

                @Override
                public String resolveOrGenerate(String headerValue) {
                    if (headerValue == null || headerValue.isBlank()) {
                        return "generated-id";
                    }
                    return headerValue;
                }

                @Override
                public Mono<Boolean> save(String requestId) {
                    return Mono.empty();
                }

                @Override
                public Mono<Boolean> exists(String requestId) {
                    return null;
                }

                @Override
                public Mono<Duration> ttl(String requestId) {
                    return null;
                }

                @Override
                public String toRedisKey(String requestId) {
                    return "";
                }
            };
        }

        @RestController
        static class EchoController {

            @GetMapping(value = "/echo", produces = MediaType.APPLICATION_JSON_VALUE)
            public Mono<Map<String, String>> echo(ServerWebExchange exchange) {

                String header = exchange.getRequest().getHeaders().getFirst("X-Request-Id");
                String attr = exchange.getAttribute(ATTR_REQUEST_ID);

                return Mono.deferContextual(ctx -> {
                    String ctxId = ctx.getOrDefault(REQUEST_ID, "n/a");
                    Map<String, String> result = new LinkedHashMap<>();
                    result.put("header", header);
                    result.put("attr", attr);
                    result.put("ctx", ctxId);
                    return Mono.just(result);
                });
            }
        }
    }
}

