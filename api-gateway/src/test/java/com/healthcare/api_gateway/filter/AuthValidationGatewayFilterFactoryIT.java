package com.healthcare.api_gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.api_gateway.config.properties.AuthValidationProperties;
import com.healthcare.api_gateway.config.properties.HeaderRequestIdProperties;
import com.healthcare.api_gateway.service.interfaces.RequestIdReactiveService;
import com.healthcare.api_gateway.utilite.ExchangeAttrs;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.healthcare.api_gateway.filter.constant.AttrKeys.USER_ID_ATTR_KEY;
import static com.healthcare.api_gateway.filter.constant.AttrKeys.USER_ROLES_ATTR_KEY;
import static com.healthcare.api_gateway.filter.support.TestDataFactory.*;
import static com.healthcare.api_gateway.filter.support.TestGatewayConstants.HEADER_REQUEST_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = AuthValidationGatewayFilterFactoryIT.TestApp.class
)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "header-request-id.name=X-Request-Id",
        "auth-validation.validate-path=/validate",
        "gateway.user-context.enabled=false"
})
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("AuthValidationGatewayFilterFactory IT")
class AuthValidationGatewayFilterFactoryIT {

    @Autowired
    WebTestClient webTestClient;

    @MockitoBean
    RequestIdReactiveService requestIdReactiveService;

    static DisposableServer authServer;
    static final AtomicReference<HttpHeaders> lastAuthHeaders = new AtomicReference<>();

    private static final String MOCK_VALIDATION_URL = "/secured";
    private static final String BEARER = "Bearer ";

    private static final String NOT_ALLOWED_HEADER = "X-Not-Allowed";

    private static final String HOST_AUTH_SERVER = "127.0.0.1";
    private static final int PORT_AUTH_SERVER = 0;

    private static Long userId = 0L;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {

        final String BEARER_OK_TOKEN = "ok";
        final String VALIDATE_PATH = "/validate";
        userId = randomUserId();

        if (authServer == null) {
            authServer = HttpServer.create()
                    .host(HOST_AUTH_SERVER)
                    .port(PORT_AUTH_SERVER)
                    .route(routes -> routes
                            .get(VALIDATE_PATH, (req, res) -> {

                                HttpHeaders headers = new HttpHeaders();
                                req.requestHeaders().forEach(e -> headers.add(e.getKey(), e.getValue()));
                                lastAuthHeaders.set(headers);

                                String auth = req.requestHeaders().get(HttpHeaders.AUTHORIZATION);
                                if (!(BEARER + BEARER_OK_TOKEN).equals(auth)) {
                                    return res.status(401).send();
                                }

                                var ctx = new AuthValidationGatewayFilterFactory.AuthContext(
                                        userId,
                                        List.of(roleUser(), roleAdmin()));

                                String json;
                                try {
                                    json = MAPPER.writeValueAsString(ctx);
                                } catch (JsonProcessingException e) {
                                    throw new RuntimeException(e);
                                }

                                return res.status(200)
                                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                        .sendString(Mono.just(json));
                            }))
                    .bindNow();
        }

        String baseUrl = "http://" + HOST_AUTH_SERVER + ":" + authServer.port();
        registry.add("auth-validation.auth-service-uri", () -> baseUrl);
    }

    @BeforeEach
    void setupRequestIdMock() {
        when(requestIdReactiveService.resolveOrGenerate(any()))
                .thenAnswer(inv -> {
                    String v = inv.getArgument(0, String.class);
                    return (v == null || v.isBlank()) ? requestId() : v;
                });

        when(requestIdReactiveService.save(any())).thenReturn(Mono.just(true));
    }

    @AfterAll
    static void stopAuthServer() {
        if (authServer != null) authServer.disposeNow();
    }

    @Test
    void when_auth_ok_should_pass_and_attrs_visible() {
        final String BEARER_OK_TOKEN = "ok";
        final String REQUEST_ID = requestId();
        final String FIELD_USER_ID = "$.userId";
        final String FIELD_ROLES = "$.roles";

        webTestClient.get()
                .uri(MOCK_VALIDATION_URL)
                .header(HttpHeaders.AUTHORIZATION, BEARER + BEARER_OK_TOKEN)
                .header(HEADER_REQUEST_ID, REQUEST_ID)
                .header(NOT_ALLOWED_HEADER, "nope")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath(FIELD_USER_ID).isEqualTo(userId)
                .jsonPath(FIELD_ROLES).value(containsInAnyOrder(roleUser(), roleAdmin()));

        HttpHeaders h = lastAuthHeaders.get();
        assertThat(h).isNotNull();
        assertThat(h.getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo(BEARER + BEARER_OK_TOKEN);
        assertThat(h.getFirst(HEADER_REQUEST_ID)).isEqualTo(REQUEST_ID);
        assertThat(h.containsKey(NOT_ALLOWED_HEADER)).isFalse();
    }

    @Test
    void when_auth_bad_should_return_401_and_stop_chain() {
        final String BEARER_BAD_TOKEN = "bad";
        final String REQUEST_ID = requestId();

        webTestClient.get()
                .uri(MOCK_VALIDATION_URL)
                .header(HttpHeaders.AUTHORIZATION, BEARER + BEARER_BAD_TOKEN)
                .header(HEADER_REQUEST_ID, REQUEST_ID)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @SpringBootApplication
    @EnableConfigurationProperties({AuthValidationProperties.class, HeaderRequestIdProperties.class})
    static class TestApp {

        @Bean
        RouteLocator routes(RouteLocatorBuilder builder,
                            AuthValidationGatewayFilterFactory authFactory,
                            AuthValidationProperties authProps) {

            AuthValidationGatewayFilterFactory.Config cfg = new AuthValidationGatewayFilterFactory.Config();
            cfg.setAuthServiceUri(authProps.authServiceUri());
            cfg.setValidatePath(authProps.validatePath());
            cfg.setMethod(HttpMethod.GET);
            cfg.setForwardHeaders(List.of(HttpHeaders.AUTHORIZATION, HEADER_REQUEST_ID));

            return builder.routes()
                    .route("secured", r -> r.path(MOCK_VALIDATION_URL)
                            .filters(f -> f.filter(authFactory.apply(cfg)))
                            .uri("forward:/echo"))
                    .build();
        }

        @RestController
        static class EchoController {

            @GetMapping(value = "/echo", produces = MediaType.APPLICATION_JSON_VALUE)
            public Mono<Map<String, Object>> echo(ServerWebExchange exchange) {

                Long userId = ExchangeAttrs.get(exchange, USER_ID_ATTR_KEY, Long.class).orElse(null);

                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) exchange.getAttributes().get(USER_ROLES_ATTR_KEY.name());

                return Mono.just(Map.of(
                        "userId", userId == null ? "null" : String.valueOf(userId),
                        "roles", roles == null ? List.of() : roles
                ));
            }
        }
    }
}