package com.healthcare.auth_service.service.feignClient.config;


import com.healthcare.auth_service.config.properties.HeaderRequestIdProperties;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Collection;
import java.util.UUID;

import static com.healthcare.auth_service.filter.context.constant
        .RequestContextAttributes.ATTR_REQUEST_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Correlation request id Feign interceptor tests: ")
class CorrelationRequestIdFeignConfigurationTest {

    private static final String HEADER_NAME =
            "X-Request-Id";

    private CorrelationRequestIdFeignConfiguration configuration;

    private HeaderRequestIdProperties properties;

    @BeforeEach
    void setUp() {
        configuration =
                new CorrelationRequestIdFeignConfiguration();

        properties =
                mock(HeaderRequestIdProperties.class);

        when(properties.name())
                .thenReturn(HEADER_NAME);
    }

    @Test
    void interceptor_ShouldForwardRequestId_WhenRequestAttributeExists() {
        UUID requestId = UUID.randomUUID();

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setAttribute(
                ATTR_REQUEST_ID,
                requestId
        );

        ObjectProvider<HttpServletRequest> requestProvider =
                requestProvider(request);

        RequestInterceptor interceptor =
                configuration.correlationRequestIdInterceptor(
                        requestProvider,
                        properties
                );

        RequestTemplate template =
                new RequestTemplate();

        interceptor.apply(template);

        assertThat(headerValues(template))
                .containsExactly(requestId.toString());
    }

    @Test
    void interceptor_ShouldNotAddHeader_WhenRequestIdAttributeIsMissing() {
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        ObjectProvider<HttpServletRequest> requestProvider =
                requestProvider(request);

        RequestInterceptor interceptor =
                configuration.correlationRequestIdInterceptor(
                        requestProvider,
                        properties
                );

        RequestTemplate template =
                new RequestTemplate();

        interceptor.apply(template);

        assertThat(template.headers())
                .doesNotContainKey(HEADER_NAME);
    }

    @Test
    void interceptor_ShouldNotAddHeader_WhenHttpRequestIsUnavailable() {
        ObjectProvider<HttpServletRequest> requestProvider =
                requestProvider(null);

        RequestInterceptor interceptor =
                configuration.correlationRequestIdInterceptor(
                        requestProvider,
                        properties
                );

        RequestTemplate template =
                new RequestTemplate();

        interceptor.apply(template);

        assertThat(template.headers())
                .doesNotContainKey(HEADER_NAME);
    }

    @Test
    void interceptor_ShouldNotAddHeader_WhenRequestIdAttributeHasWrongType() {
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setAttribute(
                ATTR_REQUEST_ID,
                UUID.randomUUID().toString()
        );

        ObjectProvider<HttpServletRequest> requestProvider =
                requestProvider(request);

        RequestInterceptor interceptor =
                configuration.correlationRequestIdInterceptor(
                        requestProvider,
                        properties
                );

        RequestTemplate template =
                new RequestTemplate();

        interceptor.apply(template);

        assertThat(template.headers())
                .doesNotContainKey(HEADER_NAME);
    }

    private Collection<String> headerValues(
            RequestTemplate template
    ) {
        return template.headers()
                .get(HEADER_NAME);
    }

    @SuppressWarnings("unchecked")
    private ObjectProvider<HttpServletRequest> requestProvider(
            HttpServletRequest request
    ) {
        ObjectProvider<HttpServletRequest> provider =
                mock(ObjectProvider.class);

        when(provider.getIfAvailable())
                .thenReturn(request);

        return provider;
    }
}