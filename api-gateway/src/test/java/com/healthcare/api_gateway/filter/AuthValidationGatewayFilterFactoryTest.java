package com.healthcare.api_gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.api_gateway.config.properties.AuthValidationProperties;
import com.healthcare.api_gateway.config.properties.HeaderRequestIdProperties;
import com.healthcare.api_gateway.utilite.ExchangeAttrs;
import org.junit.jupiter.api.*;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.healthcare.api_gateway.filter.constant.AttrKeys.USER_ID_ATTR_KEY;
import static com.healthcare.api_gateway.filter.constant.AttrKeys.USER_ROLES_ATTR_KEY;
import static com.healthcare.api_gateway.filter.support.TestDataFactory.*;
import static com.healthcare.api_gateway.filter.support.TestGatewayConstants.HEADER_REQUEST_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("AuthValidationGatewayFilterFactory unit tests")
class AuthValidationGatewayFilterFactoryTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private AuthValidationProperties authProps;
    private HeaderRequestIdProperties requestIdProps;

    private final String BEARER = "Bearer ";
    final String MOCK_VALIDATION_URL = "/secure";

    @BeforeEach
    void setUp() {
        authProps = mock(AuthValidationProperties.class);
        requestIdProps = mock(HeaderRequestIdProperties.class);

        when(authProps.authServiceUri()).thenReturn("http://auth");
        when(authProps.validatePath()).thenReturn("/api/v1/auth/validate");
        when(requestIdProps.name()).thenReturn(HEADER_REQUEST_ID);
    }

    @Test
    void should_forward_only_allowed_headers_and_store_attrs_on_200() throws Exception {

        final Long USER_ID = randomUserId();
        final String AUTH_SERVICER_URI = "http://auth-service:8081";
        final String VALIDATE_PATH = "/validate";
        final String BEARER_TOKEN = "token";
        final String REQUEST_ID = requestId();
        final String NOT_ALLOWED_HEADER = "X-Not-Allowed";

        // capture outgoing request
        AtomicReference<ClientRequest> captured = new AtomicReference<>();

        // fake auth response body
        var ctx = new AuthValidationGatewayFilterFactory.AuthContext(
                USER_ID,
                List.of(roleUser(), roleAdmin()));

        String json = MAPPER.writeValueAsString(ctx);

        WebClient.Builder builder = WebClient.builder()
                .exchangeFunction(req -> {
                    captured.set(req);
                    return Mono.just(
                            ClientResponse.create(HttpStatus.OK)
                                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                    .body(json)
                                    .build()
                    );
                });

        AuthValidationGatewayFilterFactory factory =
                new AuthValidationGatewayFilterFactory(builder, authProps, requestIdProps);

        AuthValidationGatewayFilterFactory.Config cfg = new AuthValidationGatewayFilterFactory.Config();
        cfg.setAuthServiceUri(AUTH_SERVICER_URI);     // override
        cfg.setValidatePath(VALIDATE_PATH);
        cfg.setMethod(HttpMethod.GET);
        cfg.setForwardHeaders(List.of(HttpHeaders.AUTHORIZATION, HEADER_REQUEST_ID)); // explicit

        GatewayFilter filter = factory.apply(cfg);

        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get(MOCK_VALIDATION_URL)
                        .header(HttpHeaders.AUTHORIZATION, BEARER + BEARER_TOKEN)
                        .header(HEADER_REQUEST_ID, REQUEST_ID)
                        .header(NOT_ALLOWED_HEADER, "nope")
                        .build()
        );

        CapturingChain chain = new CapturingChain();

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        // chain executed
        assertThat(chain.called).isTrue();

        // attrs stored
        assertThat(ExchangeAttrs.get(exchange, USER_ID_ATTR_KEY, Long.class)
                .orElse(null))
                .isEqualTo(USER_ID);

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) exchange.getAttributes().get(USER_ROLES_ATTR_KEY.name());

        assertThat(roles).containsExactlyInAnyOrder(roleUser(), roleAdmin());

        // verify outgoing request headers: only allowed
        ClientRequest out = captured.get();
        assertThat(out).isNotNull();
        assertThat(out.url().toString()).isEqualTo(AUTH_SERVICER_URI + VALIDATE_PATH);

        HttpHeaders outHeaders = new HttpHeaders();
        outHeaders.putAll(out.headers());

        assertThat(outHeaders.getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo(BEARER + BEARER_TOKEN);
        assertThat(outHeaders.getFirst(HEADER_REQUEST_ID)).isEqualTo(REQUEST_ID);
        assertThat(outHeaders.containsKey(NOT_ALLOWED_HEADER)).isFalse();
    }

    @Test
    void should_stop_chain_and_propagate_status_on_401() {

        final String BEARER_BAD_TOKEN = "bad";

        WebClient.Builder builder = WebClient.builder()
                .exchangeFunction(req -> Mono.just(
                        ClientResponse.create(HttpStatus.UNAUTHORIZED).build()
                ));

        AuthValidationGatewayFilterFactory factory =
                new AuthValidationGatewayFilterFactory(builder, authProps, requestIdProps);

        GatewayFilter filter = factory.apply(new AuthValidationGatewayFilterFactory.Config()); // defaults

        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get(MOCK_VALIDATION_URL)
                        .header(HttpHeaders.AUTHORIZATION, BEARER + BEARER_BAD_TOKEN)
                        .header(HEADER_REQUEST_ID, requestId())
                        .build()
        );

        CapturingChain chain = new CapturingChain();

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(chain.called).isFalse();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void should_use_defaults_when_config_empty() throws JsonProcessingException {

        final String BEARER_TOKEN = "token";
        final String REQUEST_ID = requestId();
        final Long USER_ID = randomUserId();

        var ctx = new AuthValidationGatewayFilterFactory.AuthContext(
                USER_ID,
                List.of(roleUser()));

        String json = MAPPER.writeValueAsString(ctx);

        WebClient.Builder builder = WebClient.builder()
                .exchangeFunction(req -> Mono.just(
                        ClientResponse.create(HttpStatus.OK)
                                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .body(json)
                                .build()
                ));

        AuthValidationGatewayFilterFactory factory =
                new AuthValidationGatewayFilterFactory(builder, authProps, requestIdProps);

        GatewayFilter filter = factory.apply(new AuthValidationGatewayFilterFactory.Config()); // all null -> defaults

        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get(MOCK_VALIDATION_URL)
                        .header(HttpHeaders.AUTHORIZATION, BEARER + BEARER_TOKEN)
                        .header(HEADER_REQUEST_ID, REQUEST_ID)
                        .build()
        );

        CapturingChain chain = new CapturingChain();

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(chain.called).isTrue();
        assertThat(ExchangeAttrs.get(exchange, USER_ID_ATTR_KEY, Long.class)
                .orElse(null))
                .isEqualTo(USER_ID);
    }

    static class CapturingChain implements GatewayFilterChain {
        boolean called = false;

        @Override
        public Mono<Void> filter(ServerWebExchange exchange) {
            called = true;
            return Mono.empty();
        }
    }
}