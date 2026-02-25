package com.healthcare.api_gateway.filter;

import com.healthcare.api_gateway.config.properties.UserContextProperties;
import com.healthcare.api_gateway.filter.signing.interfaces.UserContextSigner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static com.healthcare.api_gateway.filter.constant.AttrKeys.*;
import static com.healthcare.api_gateway.filter.support.TestDataFactory.*;
import static com.healthcare.api_gateway.filter.support.TestGatewayConstants.HEADER_USER_CONTEXT;
import static com.healthcare.api_gateway.filter.support.TestGatewayConstants.TEST_URI;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = AddSignedUserContextGatewayFilterFactoryIT.TestApp.class
)
@TestPropertySource(properties = {
        "gateway.user-context.enabled=true",
        "gateway.auth-validation.enabled=false",
        "gateway.request-id.enabled=false",

        "user-context.user-context-header=" + HEADER_USER_CONTEXT,
        "user-context.ttl=PT30S"
})
@ActiveProfiles("test")
class AddSignedUserContextGatewayFilterFactoryIT {

    static final String OK_URI = TEST_URI + "/user-context";
    static final String MISSING_URI = TEST_URI + "/user-context-missing";

    @Autowired
    WebTestClient webTestClient;

    @MockitoBean
    UserContextSigner signer;

    private static Long userId = 0L;
    private static String rid = null;
    private static List<String> roles = List.of(roleUser(), roleAdmin());

    private static final String USER_CONTEXT_HEADER = "userContextHeader";

    @BeforeEach
    void setUp() {
        userId = randomUserId();
        rid = requestId();
    }

    @Test
    void when_Attrs_Present_should_Add_Signed_Header() {
        String signedJws = singedJws();

        when(signer.sign(eq(userId.toString()),
                eq(roles),
                eq(rid),
                any(Duration.class)))
                .thenReturn(signedJws);

        webTestClient.get()
                .uri(OK_URI)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$." + USER_CONTEXT_HEADER).isEqualTo(signedJws);
    }

    @Test
    void when_Attrs_Missing_should_Return_401() {
        webTestClient.get()
                .uri(MISSING_URI)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @SpringBootApplication
    @EnableConfigurationProperties(UserContextProperties.class)
    @Import(TestGatewayConfig.class)
    static class TestApp {
    }

    @TestConfiguration
    static class TestGatewayConfig {

        @Bean
        RouteLocator routes(RouteLocatorBuilder builder,
                            AddSignedUserContextGatewayFilterFactory addSignedUserContext) {

            return builder.routes()

                    .route("user-context-ok", r -> r.path(OK_URI)
                            .filters(f -> f.filter(addSignedUserContext
                                    .apply(new AddSignedUserContextGatewayFilterFactory.Config())))
                            .uri("forward:/echo"))

                    .route("user-context-missing", r -> r.path(MISSING_URI)
                            .filters(f -> f.filter(addSignedUserContext
                                    .apply(new AddSignedUserContextGatewayFilterFactory.Config())))
                            .uri("forward:/echo"))

                    .build();
        }

        @Bean
        GlobalFilter putAttrsForOkRoute() {
            return new PutAttrsGlobalFilter();
        }

        @RestController
        static class EchoController {
            @GetMapping(value = "/echo", produces = MediaType.APPLICATION_JSON_VALUE)
            public Mono<Map<String, Object>> echo(ServerWebExchange exchange) {
                String header = exchange.getRequest().getHeaders().getFirst(HEADER_USER_CONTEXT);
                return Mono.just(Map.of(USER_CONTEXT_HEADER, header));
            }
        }


        static final class PutAttrsGlobalFilter implements GlobalFilter, Ordered {

            @Override
            public int getOrder() {
                return -1000;
            }

            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                if (OK_URI.equals(exchange.getRequest().getPath().value())) {
                    exchange.getAttributes().put(USER_ID_ATTR_KEY.name(), userId);
                    exchange.getAttributes().put(REQUEST_ID_ATTR_KEY.name(), rid);
                    exchange.getAttributes().put(USER_ROLES_ATTR_KEY.name(), roles);
                }
                return chain.filter(exchange);
            }
        }
    }
}