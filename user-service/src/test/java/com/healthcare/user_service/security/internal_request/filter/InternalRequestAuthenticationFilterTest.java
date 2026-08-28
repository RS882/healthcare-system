package com.healthcare.user_service.security.internal_request.filter;

import com.healthcare.user_service.config.configs_components.CustomAuthenticationEntryPoint;
import com.healthcare.user_service.exception_handler.exception.InternalRequestAuthenticationServiceException;
import com.healthcare.user_service.exception_handler.exception.InternalRequestGrantInvalidException;
import com.healthcare.user_service.exception_handler.exception.InternalRequestGrantNotFoundException;
import com.healthcare.user_service.exception_handler.exception.handler.InternalRequestAuthenticationServiceFailureHandler;
import com.healthcare.user_service.security.internal_request.authentication.InternalServiceAuthenticationToken;
import com.healthcare.user_service.security.internal_request.authentication.InternalServicePrincipal;
import com.healthcare.user_service.security.internal_request.constant.InternalAuthority;
import com.healthcare.user_service.security.internal_request.constant.InternalService;
import com.healthcare.user_service.security.internal_request.consumer.interfaces.InternalRequestGrantValidator;
import com.healthcare.user_service.security.internal_request.dto.InternalRequestGrant;
import com.healthcare.user_service.security.internal_request.interfaces.InternalRequestGrantConsumer;
import com.healthcare.user_service.security.internal_request.properties.InternalRequestConsumerProperties;
import com.healthcare.user_service.security.internal_request.resolver.InternalServiceAuthorityResolver;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Internal request authentication filter tests")
class InternalRequestAuthenticationFilterTest {

    private static final String HEADER_NAME =
            "X-Internal-Request-Id";

    private static final String INTERNAL_LOOKUP_URL =
            "/v1/users/internal/lookup";

    @Mock
    private InternalRequestGrantConsumer grantConsumer;

    @Mock
    private InternalRequestGrantValidator grantValidator;

    @Mock
    private InternalServiceAuthorityResolver authorityResolver;

    @Mock
    private InternalRequestConsumerProperties props;

    @Mock
    private CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Mock
    private InternalRequestAuthenticationServiceFailureHandler
            authenticationServiceFailureHandler;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private InternalRequestAuthenticationFilter filter;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        request =
                new MockHttpServletRequest(
                        "POST",
                        INTERNAL_LOOKUP_URL
                );

        response =
                new MockHttpServletResponse();

        when(props.headerName())
                .thenReturn(HEADER_NAME);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void should_authenticate_internal_service_and_continue_filter_chain_when_request_is_valid()
            throws Exception {

        UUID internalRequestId =
                UUID.randomUUID();

        request.addHeader(
                HEADER_NAME,
                internalRequestId.toString()
        );

        InternalRequestGrant grant =
                validGrant();

        GrantedAuthority authority =
                new SimpleGrantedAuthority(
                        InternalAuthority.USER_LOOKUP.authority()
                );


        when(grantConsumer.consume(internalRequestId))
                .thenReturn(grant);

        when(authorityResolver.resolve(InternalService.AUTH_SERVICE))
                .thenReturn(List.of(authority));

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(grantConsumer)
                .consume(internalRequestId);

        verify(grantValidator)
                .validate(grant, request);

        verify(authorityResolver)
                .resolve(InternalService.AUTH_SERVICE);

        verify(filterChain)
                .doFilter(request, response);

        verifyNoInteractions(
                authenticationEntryPoint,
                authenticationServiceFailureHandler
        );

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        assertThat(authentication)
                .isNotNull()
                .isInstanceOf(
                        InternalServiceAuthenticationToken.class
                );

        assertThat(authentication.isAuthenticated())
                .isTrue();

        assertThat(authentication.getPrincipal())
                .isInstanceOf(
                        InternalServicePrincipal.class
                );

        InternalServicePrincipal principal =
                (InternalServicePrincipal)
                        authentication.getPrincipal();

        assertThat(principal.service())
                .isEqualTo(InternalService.AUTH_SERVICE);

        assertThat(authentication.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly(
                        InternalAuthority.USER_LOOKUP.authority()
                );
    }

    @Test
    void should_call_authentication_entry_point_when_header_is_missing()
            throws Exception {

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(authenticationEntryPoint)
                .commence(
                        eq(request),
                        eq(response),
                        any(BadCredentialsException.class)
                );

        verifyNoInteractions(
                grantConsumer,
                grantValidator,
                authorityResolver,
                authenticationServiceFailureHandler
        );

        verifyNoInteractions(filterChain);

        assertThat(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        ).isNull();
    }

    @Test
    void should_call_authentication_entry_point_when_header_is_not_uuid()
            throws Exception {

        request.addHeader(
                HEADER_NAME,
                "not-a-valid-uuid"
        );

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(authenticationEntryPoint)
                .commence(
                        eq(request),
                        eq(response),
                        any(BadCredentialsException.class)
                );

        verifyNoInteractions(
                grantConsumer,
                grantValidator,
                authorityResolver,
                authenticationServiceFailureHandler
        );

        verifyNoInteractions(filterChain);

        assertThat(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        ).isNull();
    }

    @Test
    void should_call_authentication_entry_point_when_grant_is_not_found()
            throws Exception {

        UUID internalRequestId =
                UUID.randomUUID();

        request.addHeader(
                HEADER_NAME,
                internalRequestId.toString()
        );

        when(grantConsumer.consume(internalRequestId))
                .thenThrow(
                        new InternalRequestGrantNotFoundException()
                );

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(grantConsumer)
                .consume(internalRequestId);

        verify(authenticationEntryPoint)
                .commence(
                        eq(request),
                        eq(response),
                        any(InternalRequestGrantNotFoundException.class)
                );

        verifyNoInteractions(
                grantValidator,
                authorityResolver,
                authenticationServiceFailureHandler
        );

        verifyNoInteractions(filterChain);

        assertThat(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        ).isNull();
    }

    @Test
    void should_call_authentication_entry_point_when_grant_is_invalid()
            throws Exception {

        UUID internalRequestId =
                UUID.randomUUID();

        request.addHeader(
                HEADER_NAME,
                internalRequestId.toString()
        );

        InternalRequestGrant grant =
                validGrant();

        when(grantConsumer.consume(internalRequestId))
                .thenReturn(grant);

        doThrow(
                new InternalRequestGrantInvalidException(
                        "Internal request target mismatch"
                )
        )
                .when(grantValidator)
                .validate(grant, request);

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(grantConsumer)
                .consume(internalRequestId);

        verify(grantValidator)
                .validate(grant, request);

        verify(authenticationEntryPoint)
                .commence(
                        eq(request),
                        eq(response),
                        any(InternalRequestGrantInvalidException.class)
                );

        verifyNoInteractions(
                authorityResolver,
                authenticationServiceFailureHandler
        );

        verifyNoInteractions(filterChain);

        assertThat(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        ).isNull();
    }

    @Test
    void should_call_service_failure_handler_when_internal_authentication_service_is_unavailable()
            throws Exception {

        UUID internalRequestId =
                UUID.randomUUID();

        request.addHeader(
                HEADER_NAME,
                internalRequestId.toString()
        );

        InternalRequestAuthenticationServiceException exception =
                new InternalRequestAuthenticationServiceException(
                        "Internal request authentication service is unavailable",
                        new RuntimeException("Redis unavailable")
                );

        when(grantConsumer.consume(internalRequestId))
                .thenThrow(exception);

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(grantConsumer)
                .consume(internalRequestId);

        verify(authenticationServiceFailureHandler)
                .handle(
                        request,
                        response,
                        exception
                );

        verifyNoInteractions(
                authenticationEntryPoint,
                grantValidator,
                authorityResolver
        );

        verifyNoInteractions(filterChain);

        assertThat(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        ).isNull();
    }

    @Test
    void should_clear_existing_security_context_when_authentication_fails()
            throws Exception {

        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        mock(Authentication.class)
                );

        request.addHeader(
                HEADER_NAME,
                "invalid-uuid"
        );

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(authenticationEntryPoint)
                .commence(
                        eq(request),
                        eq(response),
                        any(BadCredentialsException.class)
                );

        assertThat(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        ).isNull();

        verifyNoInteractions(filterChain);
    }

    private InternalRequestGrant validGrant() {
        return InternalRequestGrant.builder()
                .issuer(InternalService.AUTH_SERVICE)
                .target("user-service")
                .method("POST")
                .path(INTERNAL_LOOKUP_URL)
                .createdAt(Instant.now())
                .build();
    }
}