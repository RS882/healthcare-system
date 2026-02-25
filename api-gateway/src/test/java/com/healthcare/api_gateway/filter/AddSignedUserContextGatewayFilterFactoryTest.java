package com.healthcare.api_gateway.filter;

import com.healthcare.api_gateway.config.properties.UserContextProperties;
import com.healthcare.api_gateway.filter.signing.interfaces.UserContextSigner;
import org.junit.jupiter.api.*;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.healthcare.api_gateway.filter.constant.AttrKeys.*;
import static com.healthcare.api_gateway.filter.support.TestDataFactory.*;
import static com.healthcare.api_gateway.filter.support.TestGatewayConstants.HEADER_USER_CONTEXT;
import static com.healthcare.api_gateway.filter.support.TestGatewayConstants.TEST_URI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("AddSignedUserContext Gateway Filter Factory unit tests")
class AddSignedUserContextGatewayFilterFactoryTest {

    private UserContextProperties userContextProps;
    private UserContextSigner signer;
    private AddSignedUserContextGatewayFilterFactory factory;
    private AddSignedUserContextGatewayFilterFactory.Config cfg;

    @BeforeEach
    void setUp() {
        userContextProps = mock(UserContextProperties.class);
        signer = mock(UserContextSigner.class);

        factory = new AddSignedUserContextGatewayFilterFactory(userContextProps, signer);

        cfg = new AddSignedUserContextGatewayFilterFactory.Config();

        when(userContextProps.userContextHeader()).thenReturn(HEADER_USER_CONTEXT);
    }

    @Test
    void apply_shouldAddSignedHeader_andCallChain_whenAttrsPresent() {

        final String TEST_SIGNED_JWS = "signed-jws";
        final Duration USER_CONTEXT_TTL = Duration.ofSeconds(10);
        final Long USER_ID = randomUserId();
        final String REQUEST_ID = requestId();
        final List<String> USER_ROLES = List.of(roleUser(), roleAdmin());

        when(signer.sign(anyString(), anyList(), anyString(), any(Duration.class)))
                .thenReturn(TEST_SIGNED_JWS);

        cfg.setUserContextHeader(null); // take from props
        cfg.setTtl(USER_CONTEXT_TTL);

        GatewayFilter filter = factory.apply(cfg);

        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get(TEST_URI).build()
        );
        exchange.getAttributes().put(USER_ID_ATTR_KEY.name(), USER_ID);
        exchange.getAttributes().put(REQUEST_ID_ATTR_KEY.name(), REQUEST_ID);
        exchange.getAttributes().put(USER_ROLES_ATTR_KEY.name(), USER_ROLES);

        AtomicReference<ServerWebExchange> captured = new AtomicReference<>();
        GatewayFilterChain chain = ex -> {
            captured.set(ex);
            return Mono.empty();
        };

        // when
        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        // then
        verify(signer, times(1)).sign(
                eq(USER_ID.toString()),
                eq(USER_ROLES),
                eq(REQUEST_ID),
                eq(USER_CONTEXT_TTL)
        );

        ServerWebExchange mutated = captured.get();
        assertThat(mutated).isNotNull();

        ServerHttpRequest mutatedReq = mutated.getRequest();
        assertThat(mutatedReq.getHeaders().getFirst(HEADER_USER_CONTEXT))
                .isEqualTo(TEST_SIGNED_JWS);

        // status should remain untouched (not 401)
        assertThat(exchange.getResponse().getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void apply_shouldReturn401_andComplete_whenUserIdMissing() {

        final Duration USER_CONTEXT_TTL = Duration.ofSeconds(10);
        final String REQUEST_ID = requestId();
        final List<String> USER_ROLES = List.of(roleUser());

        cfg.setTtl(USER_CONTEXT_TTL);

        GatewayFilter filter = factory.apply(cfg);

        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get(TEST_URI).build()
        );

        // missing USER_ID_ATTR_KEY
        exchange.getAttributes().put(REQUEST_ID_ATTR_KEY.name(), REQUEST_ID);
        exchange.getAttributes().put(USER_ROLES_ATTR_KEY.name(), USER_ROLES);

        AtomicBoolean chainCalled = new AtomicBoolean(false);
        GatewayFilterChain chain = ex -> {
            chainCalled.set(true);
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exchange.getResponse().isCommitted()).isTrue();

        verifyNoInteractions(signer);
        assertThat(chainCalled.get()).isFalse();
    }

    @Test
    void apply_shouldReturn401_andComplete_whenRequestIdMissingOrBlank() {

        final Duration USER_CONTEXT_TTL = Duration.ofSeconds(10);
        final Long USER_ID = randomUserId();

        cfg.setTtl(USER_CONTEXT_TTL);

        GatewayFilter filter = factory.apply(cfg);

        // --- case 1: missing requestId ---
        ServerWebExchange exchange1 = MockServerWebExchange.from(
                MockServerHttpRequest.get(TEST_URI).build()
        );
        exchange1.getAttributes().put(USER_ID_ATTR_KEY.name(), USER_ID);
        // missing REQUEST_ID_ATTR_KEY

        StepVerifier.create(filter.filter(exchange1, ex -> Mono.empty()))
                .verifyComplete();

        assertThat(exchange1.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verifyNoInteractions(signer);

        reset(signer);

        // --- case 2: blank requestId ---
        ServerWebExchange exchange2 = MockServerWebExchange.from(
                MockServerHttpRequest.get(TEST_URI).build()
        );
        exchange2.getAttributes().put(USER_ID_ATTR_KEY.name(), USER_ID);
        exchange2.getAttributes().put(REQUEST_ID_ATTR_KEY.name(), "   "); // blank

        StepVerifier.create(filter.filter(exchange2, ex -> Mono.empty()))
                .verifyComplete();

        assertThat(exchange2.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verifyNoInteractions(signer);
    }

    @Test
    void apply_shouldFallbackTtl_whenConfigTtlNullOrNonPositive() {

        final String TEST_SIGNED_JWS = "signed-jws";
        final Duration USER_CONTEXT_TTL = Duration.ofSeconds(5);
        final Long USER_ID = randomUserId();
        final String REQUEST_ID = requestId();
        final List<String> USER_ROLES = List.of(roleAdmin());

        when(userContextProps.ttl()).thenReturn(USER_CONTEXT_TTL); // fallback

        when(signer.sign(anyString(), anyList(), anyString(), any(Duration.class)))
                .thenReturn(TEST_SIGNED_JWS);

        cfg.setTtl(Duration.ZERO); // non-positive => fallback props.ttl()

        GatewayFilter filter = factory.apply(cfg);

        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get(TEST_URI).build()
        );
        exchange.getAttributes().put(USER_ID_ATTR_KEY.name(), USER_ID);
        exchange.getAttributes().put(REQUEST_ID_ATTR_KEY.name(), REQUEST_ID);
        exchange.getAttributes().put(USER_ROLES_ATTR_KEY.name(), USER_ROLES);

        StepVerifier.create(filter.filter(exchange, ex -> Mono.empty()))
                .verifyComplete();

        verify(signer).sign(
                eq(USER_ID.toString()),
                eq(USER_ROLES),
                eq(REQUEST_ID),
                eq(USER_CONTEXT_TTL));
    }

    @Test
    void apply_shouldUseDefault30sTtl_whenPropsTtlInvalid_andConfigTtlInvalid() {

        final String TEST_SIGNED_JWS = "signed-jws";
        final Duration DEFAULT_USER_CONTEXT_TTL = Duration.ofSeconds(30);
        final Long USER_ID = randomUserId();
        final String REQUEST_ID = requestId();
        final List<String> USER_ROLES = List.of(roleAdmin());

        when(userContextProps.ttl()).thenReturn(Duration.ZERO); // invalid => default 30s
        when(signer.sign(anyString(), anyList(), anyString(), any(Duration.class)))
                .thenReturn(TEST_SIGNED_JWS);

        cfg.setTtl(Duration.ofSeconds(-1)); // invalid => fallback/default

        GatewayFilter filter = factory.apply(cfg);

        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get(TEST_URI).build()
        );
        exchange.getAttributes().put(USER_ID_ATTR_KEY.name(),USER_ID);
        exchange.getAttributes().put(REQUEST_ID_ATTR_KEY.name(), REQUEST_ID);
        exchange.getAttributes().put(USER_ROLES_ATTR_KEY.name(), USER_ROLES);

        StepVerifier.create(filter.filter(exchange, ex -> Mono.empty()))
                .verifyComplete();

        verify(signer).sign(
                eq(USER_ID.toString()),
                eq(USER_ROLES),
                eq(REQUEST_ID),
                eq(DEFAULT_USER_CONTEXT_TTL));
    }
}