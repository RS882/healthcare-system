package com.healthcare.aiservice.security.internal_request;

import com.healthcare.aiservice.security.internal_request.interfaces.InternalRequestGrantIssuer;
import com.healthcare.aiservice.security.internal_request.properties.InternalRequestIssuerProperties;
import com.healthcare.aiservice.security.internal_request.properties.UserServiceInternalClientProperties;
import com.healthcare.aiservice.security.properties.HeaderRequestIdProperties;
import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.util.Collection;
import java.util.UUID;

import static com.healthcare.aiservice.security.filter.security.constant.AttrNames.ATTR_REQUEST_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Internal request Feign interceptor tests: ")
class InternalRequestFeignInterceptorTest {

    private static final String ISSUER =
            "ai-service";

    private static final String KEY_PREFIX =
            "internal-request:";

    private static final String INTERNAL_REQUEST_HEADER_NAME =
            "X-Internal-Request-Id";

    private static final String REQUEST_ID_HEADER_NAME =
            "X-Request-Id";

    private static final Duration TTL =
            Duration.ofSeconds(30);

    private static final String TARGET_SERVICE =
            "user-service";

    private static final String BASE_PATH =
            "/api/v1/users/internal";

    private static final String TEMPLATE_PATH =
            "/42/auth-info";

    private static final String EXPECTED_GRANT_PATH =
            "/api/v1/users/internal/42/auth-info";

    @Mock
    private InternalRequestGrantIssuer grantIssuer;

    private InternalRequestFeignInterceptor interceptor;

    @BeforeEach
    void setUp() {

        InternalRequestIssuerProperties issuerProperties =
                new InternalRequestIssuerProperties(
                        ISSUER,
                        KEY_PREFIX,
                        INTERNAL_REQUEST_HEADER_NAME,
                        TTL
                );

        UserServiceInternalClientProperties clientProperties =
                new UserServiceInternalClientProperties(
                        TARGET_SERVICE,
                        BASE_PATH
                );

        HeaderRequestIdProperties requestIdProperties =
                new HeaderRequestIdProperties(
                        REQUEST_ID_HEADER_NAME
                );

        interceptor =
                new InternalRequestFeignInterceptor(
                        grantIssuer,
                        issuerProperties,
                        clientProperties,
                        requestIdProperties
                );
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void apply_ShouldIssueGrant_AndAddInternalRequestIdHeader() {

        UUID internalRequestId =
                UUID.randomUUID();

        when(grantIssuer.issue(
                TARGET_SERVICE,
                HttpMethod.GET,
                EXPECTED_GRANT_PATH
        )).thenReturn(internalRequestId);

        RequestTemplate template =
                createGetRequestTemplate();

        interceptor.apply(template);

        verify(grantIssuer, times(1))
                .issue(
                        TARGET_SERVICE,
                        HttpMethod.GET,
                        EXPECTED_GRANT_PATH
                );

        Collection<String> headerValues =
                template.headers()
                        .get(INTERNAL_REQUEST_HEADER_NAME);

        assertThat(headerValues)
                .containsExactly(
                        internalRequestId.toString()
                );
    }

    @Test
    void apply_ShouldPropagateValidatedRequestId() {

        UUID internalRequestId =
                UUID.randomUUID();

        String requestId =
                UUID.randomUUID().toString();

        when(grantIssuer.issue(
                TARGET_SERVICE,
                HttpMethod.GET,
                EXPECTED_GRANT_PATH
        )).thenReturn(internalRequestId);

        setCurrentRequestId(requestId);

        RequestTemplate template =
                createGetRequestTemplate();

        interceptor.apply(template);

        assertThat(
                template.headers()
                        .get(REQUEST_ID_HEADER_NAME)
        ).containsExactly(requestId);

        assertThat(
                template.headers()
                        .get(INTERNAL_REQUEST_HEADER_NAME)
        ).containsExactly(
                internalRequestId.toString()
        );
    }

    @Test
    void apply_ShouldNotAddRequestIdHeader_WhenCurrentHttpRequestIsMissing() {

        UUID internalRequestId =
                UUID.randomUUID();

        when(grantIssuer.issue(
                TARGET_SERVICE,
                HttpMethod.GET,
                EXPECTED_GRANT_PATH
        )).thenReturn(internalRequestId);

        RequestContextHolder.resetRequestAttributes();

        RequestTemplate template =
                createGetRequestTemplate();

        interceptor.apply(template);

        assertThat(
                template.headers()
                        .get(REQUEST_ID_HEADER_NAME)
        ).isNull();

        assertThat(
                template.headers()
                        .get(INTERNAL_REQUEST_HEADER_NAME)
        ).containsExactly(
                internalRequestId.toString()
        );
    }

    @Test
    void apply_ShouldNotAddRequestIdHeader_WhenRequestIdAttributeIsMissing() {

        UUID internalRequestId =
                UUID.randomUUID();

        when(grantIssuer.issue(
                TARGET_SERVICE,
                HttpMethod.GET,
                EXPECTED_GRANT_PATH
        )).thenReturn(internalRequestId);

        setCurrentRequestWithoutRequestId();

        RequestTemplate template =
                createGetRequestTemplate();

        interceptor.apply(template);

        assertThat(
                template.headers()
                        .get(REQUEST_ID_HEADER_NAME)
        ).isNull();

        assertThat(
                template.headers()
                        .get(INTERNAL_REQUEST_HEADER_NAME)
        ).containsExactly(
                internalRequestId.toString()
        );
    }

    @Test
    void apply_ShouldNotAddRequestIdHeader_WhenRequestIdAttributeIsBlank() {

        UUID internalRequestId =
                UUID.randomUUID();

        when(grantIssuer.issue(
                TARGET_SERVICE,
                HttpMethod.GET,
                EXPECTED_GRANT_PATH
        )).thenReturn(internalRequestId);

        setCurrentRequestId("   ");

        RequestTemplate template =
                createGetRequestTemplate();

        interceptor.apply(template);

        assertThat(
                template.headers()
                        .get(REQUEST_ID_HEADER_NAME)
        ).isNull();

        assertThat(
                template.headers()
                        .get(INTERNAL_REQUEST_HEADER_NAME)
        ).containsExactly(
                internalRequestId.toString()
        );
    }

    @Test
    void apply_ShouldIssueFreshGrantForEveryRequest_AndPreserveSameRequestId() {

        UUID firstInternalRequestId =
                UUID.randomUUID();

        UUID secondInternalRequestId =
                UUID.randomUUID();

        String requestId =
                UUID.randomUUID().toString();

        when(grantIssuer.issue(
                TARGET_SERVICE,
                HttpMethod.GET,
                EXPECTED_GRANT_PATH
        ))
                .thenReturn(
                        firstInternalRequestId,
                        secondInternalRequestId
                );

        setCurrentRequestId(requestId);

        RequestTemplate firstTemplate =
                createGetRequestTemplate();

        RequestTemplate secondTemplate =
                createGetRequestTemplate();

        interceptor.apply(firstTemplate);
        interceptor.apply(secondTemplate);

        verify(grantIssuer, times(2))
                .issue(
                        TARGET_SERVICE,
                        HttpMethod.GET,
                        EXPECTED_GRANT_PATH
                );

        assertThat(
                firstTemplate.headers()
                        .get(INTERNAL_REQUEST_HEADER_NAME)
        ).containsExactly(
                firstInternalRequestId.toString()
        );

        assertThat(
                secondTemplate.headers()
                        .get(INTERNAL_REQUEST_HEADER_NAME)
        ).containsExactly(
                secondInternalRequestId.toString()
        );

        assertThat(firstInternalRequestId)
                .isNotEqualTo(secondInternalRequestId);

        assertThat(
                firstTemplate.headers()
                        .get(REQUEST_ID_HEADER_NAME)
        ).containsExactly(requestId);

        assertThat(
                secondTemplate.headers()
                        .get(REQUEST_ID_HEADER_NAME)
        ).containsExactly(requestId);
    }

    private void setCurrentRequestId(
            String requestId
    ) {
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setAttribute(
                ATTR_REQUEST_ID,
                requestId
        );

        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(request)
        );
    }

    private void setCurrentRequestWithoutRequestId() {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(request)
        );
    }

    private RequestTemplate createGetRequestTemplate() {

        RequestTemplate template =
                new RequestTemplate();

        template.method(
                HttpMethod.GET.name()
        );

        template.uri(
                TEMPLATE_PATH
        );

        return template;
    }
}