package com.healthcare.api_gateway.filter;

import com.healthcare.api_gateway.config.properties.HeaderRequestIdProperties;
import com.healthcare.api_gateway.service.interfaces.RequestIdReactiveService;
import org.junit.jupiter.api.*;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.healthcare.api_gateway.filter.constant.AttrKeys.REQUEST_ID_ATTR_KEY;
import static com.healthcare.api_gateway.filter.constant.RequestIdContextKeys.REQUEST_ID_CONTEXT_KEY_NAME;
import static com.healthcare.api_gateway.filter.support.TestGatewayConstants.HEADER_REQUEST_ID;
import static com.healthcare.api_gateway.filter.support.TestGatewayConstants.TEST_PATH;
import static com.healthcare.api_gateway.filter.support.TestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Request ID global filter tests")
class RequestIdGlobalFilterTest {

    private RequestIdReactiveService requestIdService;
    private HeaderRequestIdProperties props;
    private RequestIdGlobalFilter filter;

    @BeforeEach
    void setUp() {
        requestIdService = mock(RequestIdReactiveService.class);
        props = mock(HeaderRequestIdProperties.class);
        filter = new RequestIdGlobalFilter(requestIdService, props);

        when(props.name()).thenReturn(HEADER_REQUEST_ID);
    }

    @Test
    void when_header_missing_should_generate_set_header_attr_and_context() {

        String generatedRid = requestId();

        when(requestIdService.resolveOrGenerate(null)).thenReturn(generatedRid);
        when(requestIdService.save(generatedRid)).thenReturn(Mono.just(true));

        ServerWebExchange exchange = createExchange(null);
        CapturingChain chain = new CapturingChain();

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertPropagatedRid(chain, generatedRid);

        verify(requestIdService).save(generatedRid);
        verify(requestIdService).resolveOrGenerate(null);
    }

    @Test
    void when_header_present_should_trim_and_reuse() {

        String clientRid = requestId();
        String clientRidWithSpaces = "  " + clientRid + "  ";

        when(requestIdService.resolveOrGenerate(clientRid)).thenReturn(clientRid);
        when(requestIdService.save(clientRid)).thenReturn(Mono.just(true));

        ServerWebExchange exchange = createExchange(clientRidWithSpaces);
        CapturingChain chain = new CapturingChain();

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertPropagatedRid(chain, clientRid);

        verify(requestIdService).resolveOrGenerate(clientRid);
        verify(requestIdService).save(clientRid);
    }

    @Test
    void when_header_blank_should_treat_as_missing() {

        String generatedRid = requestId();

        when(requestIdService.resolveOrGenerate(null)).thenReturn(generatedRid);
        when(requestIdService.save(generatedRid)).thenReturn(Mono.just(true));

        ServerWebExchange exchange = createExchange("   ");
        CapturingChain chain = new CapturingChain();

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertPropagatedRid(chain, generatedRid);

        verify(requestIdService).resolveOrGenerate(null);
        verify(requestIdService).save(generatedRid);
    }

    @Test
    void when_header_too_long_should_ignore_and_treat_as_missing() {

        String tooLong = headerTooLong(129);
        String generatedRid = requestId();

        when(requestIdService.resolveOrGenerate(null)).thenReturn(generatedRid);
        when(requestIdService.save(generatedRid)).thenReturn(Mono.just(true));

        ServerWebExchange exchange = createExchange(tooLong);
        CapturingChain chain = new CapturingChain();

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertPropagatedRid(chain, generatedRid);

        verify(requestIdService).resolveOrGenerate(null);
        verify(requestIdService).save(generatedRid);
    }

    @Test
    void when_save_errors_should_continue_chain() {

        String generatedRid = requestId();

        when(requestIdService.resolveOrGenerate(null)).thenReturn(generatedRid);
        when(requestIdService.save(generatedRid))
                .thenReturn(Mono.error(new RuntimeException("redis down")));

        ServerWebExchange exchange = createExchange(null);
        CapturingChain chain = new CapturingChain();

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertPropagatedRid(chain, generatedRid);

        verify(requestIdService).save(generatedRid);
    }

    // ============================================================
    // Assertions
    // ============================================================

    private static void assertPropagatedRid(CapturingChain chain, String expectedRid) {
        assertThat(chain.capturedExchange).isNotNull();

        String header = chain.capturedExchange.getRequest().getHeaders().getFirst(HEADER_REQUEST_ID);
        String attr = chain.capturedExchange.getAttribute(REQUEST_ID_ATTR_KEY.name());
        String ctx = chain.capturedRequestIdFromContext;

        assertThat(header).isEqualTo(expectedRid);
        assertThat(attr).isEqualTo(expectedRid);
        assertThat(ctx).isEqualTo(expectedRid);
    }

    // ============================================================
    // Helpers
    // ============================================================

    private static ServerWebExchange createExchange(String headerValue) {
        MockServerHttpRequest.BaseBuilder<?> builder = MockServerHttpRequest.get(TEST_PATH);

        if (headerValue != null) {
            builder.header(HEADER_REQUEST_ID, headerValue);
        }

        return MockServerWebExchange.from(builder.build());
    }

    // ============================================================
    // CapturingChain
    // ============================================================

    static class CapturingChain implements GatewayFilterChain {

        ServerWebExchange capturedExchange;
        String capturedRequestIdFromContext;

        @Override
        public Mono<Void> filter(ServerWebExchange exchange) {
            this.capturedExchange = exchange;

            return Mono.deferContextual(ctx -> {
                this.capturedRequestIdFromContext =
                        ctx.getOrDefault(REQUEST_ID_CONTEXT_KEY_NAME, "n/a");
                return Mono.empty();
            });
        }
    }
}