package com.healthcare.api_gateway.filter;


import com.healthcare.api_gateway.config.properties.AuthValidationProperties;
import com.healthcare.api_gateway.config.properties.HeaderRequestIdProperties;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class AuthValidationGatewayFilterFactoryTest {

    private MockWebServer mockWebServer;

    private AuthValidationGatewayFilterFactory factory;

    private final AuthValidationProperties authProps =
            new AuthValidationProperties("http://localhost",
                    "/api/v1/auth/validation",
                    "X-User-Id",
                    "X-User-Roles");

    private final HeaderRequestIdProperties requestIdProps =
            new HeaderRequestIdProperties("X-Request-Id");

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();

        AuthValidationProperties propsWithRealUrl =
                new AuthValidationProperties(baseUrl.substring(0, baseUrl.length() - 1),
                        "/api/v1/auth/validation",
                        "X-User-Id",
                        "X-User-Roles");

        WebClient.Builder builder = WebClient.builder();

        factory = new AuthValidationGatewayFilterFactory(builder, propsWithRealUrl, requestIdProps);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    void should_add_headers_and_continue_when_auth_returns_200() throws Exception {

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {"userId":42,"roles":["ADMIN","USER"]}
                        """));

        AuthValidationGatewayFilterFactory.Config cfg = new AuthValidationGatewayFilterFactory.Config();
        cfg.setForwardHeaders(List.of("authorization", "x-request-id"));

        GatewayFilter filter = factory.apply(cfg);

        String reqId = "abc-123";
        String token = "Bearer test-token";

        ServerWebExchange exchange = exchange(
                MockServerHttpRequest.get("/api/v1/users/id/2")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .header("X-Request-Id", reqId)
                        .header("X-Other", "SHOULD_NOT_FORWARD")
                        .build()
        );

        AtomicBoolean chainCalled = new AtomicBoolean(false);
        AtomicReference<ServerWebExchange> exchangeInChain = new AtomicReference<>();

        GatewayFilterChain chain = ex -> {
            chainCalled.set(true);
            exchangeInChain.set(ex);
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(chainCalled.get()).isTrue();

        ServerWebExchange ex2 = exchangeInChain.get();
        assertThat(ex2.getRequest().getHeaders().getFirst("X-User-Id")).isEqualTo("42");
        assertThat(ex2.getRequest().getHeaders().getFirst("X-User-Roles")).isEqualTo("ADMIN,USER");

        RecordedRequest recorded = mockWebServer.takeRequest();
        assertThat(recorded.getPath()).isEqualTo("/api/v1/auth/validation");
        assertThat(recorded.getHeader("Authorization")).isEqualTo(token);
        assertThat(recorded.getHeader("X-Request-Id")).isEqualTo(reqId);
        assertThat(recorded.getHeader("X-Other")).isNull();
    }

    @Test
    void should_return_401_and_not_call_chain_when_auth_returns_401() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(401));

        AuthValidationGatewayFilterFactory.Config cfg = new AuthValidationGatewayFilterFactory.Config();
        cfg.setForwardHeaders(List.of("authorization", "x-request-id"));

        GatewayFilter filter = factory.apply(cfg);

        ServerWebExchange exchange = exchange(
                MockServerHttpRequest.get("/api/v1/users/id/2")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer bad")
                        .header("X-Request-Id", "r1")
                        .build()
        );

        AtomicBoolean chainCalled = new AtomicBoolean(false);
        GatewayFilterChain chain = ex -> {
            chainCalled.set(true);
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(chainCalled.get()).isFalse();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void should_return_403_and_not_call_chain_when_auth_returns_403() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(403));

        AuthValidationGatewayFilterFactory.Config cfg = new AuthValidationGatewayFilterFactory.Config();
        cfg.setForwardHeaders(List.of("authorization", "x-request-id"));

        GatewayFilter filter = factory.apply(cfg);

        ServerWebExchange exchange = exchange(
                MockServerHttpRequest.get("/api/v1/users/id/2")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer no-role")
                        .header("X-Request-Id", "r2")
                        .build()
        );

        AtomicBoolean chainCalled = new AtomicBoolean(false);
        GatewayFilterChain chain = ex -> {
            chainCalled.set(true);
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(chainCalled.get()).isFalse();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private ServerWebExchange exchange(MockServerHttpRequest request) {
        return MockServerWebExchange.from(request);
    }
}
