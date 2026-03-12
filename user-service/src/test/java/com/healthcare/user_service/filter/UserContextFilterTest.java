package com.healthcare.user_service.filter;

import com.healthcare.user_service.config.properties.UserContextProperties;
import com.healthcare.user_service.filter.security.SignedUserContext;
import com.healthcare.user_service.filter.security.interfaces.UserContextVerifier;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static com.healthcare.user_service.filter.security.constant.AttrNames.ATTR_USER_CONTEXT;
import static com.healthcare.user_service.support.TestConstants.HEADER_USER_CONTEXT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("User context filter tests: ")
class UserContextFilterTest {

    private static final String HEADER_NAME = HEADER_USER_CONTEXT;
    private static final String TOKEN = "jwt.token.value";

    @Mock
    private UserContextVerifier verifier;

    @Mock
    private UserContextProperties userContextProps;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private UserContextFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void should_not_verify_and_not_set_attribute_when_header_is_null_or_blank(String headerValue)
            throws ServletException, IOException {

        when(userContextProps.userContextHeader()).thenReturn(HEADER_NAME);

        if (headerValue != null) {
            request.addHeader(HEADER_NAME, headerValue);
        }

        filter.doFilterInternal(request, response, filterChain);

        assertNull(request.getAttribute(ATTR_USER_CONTEXT));

        verifyNoInteractions(verifier);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void should_verify_token_and_set_attribute_when_header_present()
            throws ServletException, IOException {

        Claims claims = mock(Claims.class);
        SignedUserContext signedUserContext = mock(SignedUserContext.class);

        when(userContextProps.userContextHeader()).thenReturn(HEADER_NAME);
        request.addHeader(HEADER_NAME, TOKEN);
        when(verifier.verifyAndGetClaims(TOKEN)).thenReturn(claims);

        try (MockedStatic<SignedUserContext> mockedStatic = mockStatic(SignedUserContext.class)) {
            mockedStatic.when(() -> SignedUserContext.from(claims)).thenReturn(signedUserContext);

            filter.doFilterInternal(request, response, filterChain);

            verify(verifier).verifyAndGetClaims(TOKEN);
            mockedStatic.verify(() -> SignedUserContext.from(claims));
        }

        Object attr = request.getAttribute(ATTR_USER_CONTEXT);
        assertNotNull(attr);
        assertSame(signedUserContext, attr);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void should_strip_token_before_verification()
            throws ServletException, IOException {

        Claims claims = mock(Claims.class);
        SignedUserContext signedUserContext = mock(SignedUserContext.class);

        when(userContextProps.userContextHeader()).thenReturn(HEADER_NAME);
        request.addHeader(HEADER_NAME, "   " + TOKEN + "   ");
        when(verifier.verifyAndGetClaims(TOKEN)).thenReturn(claims);

        try (MockedStatic<SignedUserContext> mockedStatic = mockStatic(SignedUserContext.class)) {
            mockedStatic.when(() -> SignedUserContext.from(claims)).thenReturn(signedUserContext);

            filter.doFilterInternal(request, response, filterChain);

            verify(verifier).verifyAndGetClaims(TOKEN);
            mockedStatic.verify(() -> SignedUserContext.from(claims));
        }

        assertSame(signedUserContext, request.getAttribute(ATTR_USER_CONTEXT));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void should_throw_exception_and_not_continue_chain_when_verifier_fails()
            throws ServletException, IOException {

        RuntimeException exception = new RuntimeException("invalid token");

        when(userContextProps.userContextHeader()).thenReturn(HEADER_NAME);
        request.addHeader(HEADER_NAME, TOKEN);
        when(verifier.verifyAndGetClaims(TOKEN)).thenThrow(exception);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> filter.doFilterInternal(request, response, filterChain)
        );

        assertSame(exception, thrown);
        assertNull(request.getAttribute(ATTR_USER_CONTEXT));

        verify(verifier).verifyAndGetClaims(TOKEN);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void should_not_filter_for_actuator_path() {
        request.setServletPath("/actuator/health");

        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void should_not_filter_for_swagger_ui_path() {
        request.setServletPath("/swagger-ui/index.html");

        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void should_not_filter_for_api_docs_path() {
        request.setServletPath("/v3/api-docs");

        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void should_filter_for_regular_path() {
        request.setServletPath("/api/users/me");

        assertFalse(filter.shouldNotFilter(request));
    }
}