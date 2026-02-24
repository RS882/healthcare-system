package com.healthcare.api_gateway.filter;

import com.healthcare.api_gateway.config.properties.AuthValidationProperties;
import com.healthcare.api_gateway.config.properties.HeaderRequestIdProperties;
import com.healthcare.api_gateway.service.interfaces.RequestIdReactiveService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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

import static com.healthcare.api_gateway.filter.constant.ContextAttrNames.ATTR_REQUEST_ID;
import static com.healthcare.api_gateway.filter.constant.RequestIdContextKeys.REQUEST_ID_CONTEXT_KEY_NAME;
import static com.healthcare.api_gateway.filter.support.TestDataFactory.contextDefaultValue;
import static com.healthcare.api_gateway.filter.support.TestDataFactory.requestId;
import static com.healthcare.api_gateway.filter.support.TestGatewayConstants.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = RequestIdGlobalFilterIT.TestApp.class
)
@TestPropertySource(properties = {
        "gateway.user-context.enabled=false",

        "header-request-id.name=Test-X-Request-Id",

        "auth-validation.auth-service-uri=lb://auth-service",
        "auth-validation.validate-path=/api/v1/auth/validation",

        "request-id.prefix=testPrefix:",
        "request-id.ttl=PT30S",
        "request-id.value=test"
})
@ActiveProfiles("test")
@DisplayName("Request ID global filter integration tests: ")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class RequestIdGlobalFilterIT {

    @Autowired
    WebTestClient webTestClient;

    private static String generatedRid = null;

    private final String FIELD_HEADER="$.header";
    private final String FIELD_ATTR="$.attr";
    private final String FIELD_CTX="$.ctx";

    @BeforeEach
    void setUp() {
        generatedRid = requestId();
    }

    @Test
    void when_Header_Missing_should_Generate_And_Propagate_To_HeaderAttrAndContext() {
        webTestClient.get()
                .uri(TEST_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath(FIELD_HEADER).isEqualTo(generatedRid)
                .jsonPath(FIELD_ATTR).isEqualTo(generatedRid)
                .jsonPath(FIELD_CTX).isEqualTo(generatedRid);
    }

    @Test
    void when_Header_Present_should_Reuse_And_Propagate_To_HeaderAttrAndContext() {

        String clientRid = requestId();

        webTestClient.get()
                .uri(TEST_PATH)
                .header(HEADER_REQUEST_ID, clientRid)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath(FIELD_HEADER).isEqualTo(clientRid)
                .jsonPath(FIELD_ATTR).isEqualTo(clientRid)
                .jsonPath(FIELD_CTX).isEqualTo(clientRid);
    }

    @SpringBootApplication
    @EnableConfigurationProperties({
            HeaderRequestIdProperties.class,
            AuthValidationProperties.class
    })
    static class TestApp {

        @Bean
        RouteLocator testRoutes(RouteLocatorBuilder builder) {
            return builder.routes()
                    .route("test-route", r -> r.path(TEST_PATH)
                            .uri("forward:/echo"))
                    .build();
        }

        @Bean
        RequestIdReactiveService requestIdReactiveService() {
            return new RequestIdReactiveService() {

                @Override
                public boolean isValidUuid(String value) {
                    return true;
                }

                @Override
                public String resolveOrGenerate(String headerValue) {
                    if (headerValue == null || headerValue.isBlank()) {
                        return generatedRid;
                    }
                    return headerValue;
                }

                @Override
                public Mono<Boolean> save(String requestId) {
                    return Mono.just(true);
                }

                @Override
                public Mono<Boolean> exists(String requestId) {
                    return Mono.just(false);
                }

                @Override
                public Mono<Duration> ttl(String requestId) {
                    return Mono.just(Duration.ZERO);
                }

                @Override
                public String toRedisKey(String requestId) {
                    return REDIS_REQUEST_ID_PREFIX + requestId;
                }
            };
        }

        @RestController
        static class EchoController {

            @GetMapping(value = "/echo", produces = MediaType.APPLICATION_JSON_VALUE)
            public Mono<Map<String, String>> echo(ServerWebExchange exchange) {

                String header = exchange.getRequest().getHeaders().getFirst(HEADER_REQUEST_ID);
                String attr = exchange.getAttribute(ATTR_REQUEST_ID);

                return Mono.deferContextual(ctx -> {
                    String ctxId = ctx.getOrDefault(REQUEST_ID_CONTEXT_KEY_NAME, contextDefaultValue());
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