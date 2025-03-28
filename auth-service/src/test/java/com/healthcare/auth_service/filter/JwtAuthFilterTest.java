package com.healthcare.auth_service.filter;

import com.healthcare.auth_service.domain.AuthUserDetails;
import com.healthcare.auth_service.service.CustomUserDetailsService;
import com.healthcare.auth_service.service.JwtService;
import com.healthcare.auth_service.service.interfacies.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(value = DisplayNameGenerator.ReplaceUnderscores.class)
class JwtAuthFilterTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private CustomUserDetailsService userDetailsService;
    @Mock
    private TokenBlacklistService tokenBlacklistService;
    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    private final String VALID_TOKEN = "valid.jwt.token";
    private final String USER_EMAIL = "user@example.com";
    private final String PASSWORD = "password";
    private final Long USER_ID = 1L;
    private final String USER_ROLE = "ROLE_TEST";

    private MockHttpServletRequest request ;
    private MockHttpServletResponse response ;

    private AuthUserDetails userDetails;

    private void addTokenToRequest(MockHttpServletRequest request ){
        request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
    }

    @BeforeEach
    void setUp() {

        userDetails = new AuthUserDetails(
                USER_ID,
                USER_EMAIL,
                PASSWORD,
                List.of(new SimpleGrantedAuthority(USER_ROLE)),
                true
        );

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        SecurityContextHolder.clearContext();
    }

    @Test
    void negative_should_continue_filter_if_no_token() throws Exception {

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void negative_should_continue_filter_if_token_blacklisted() throws Exception {

        addTokenToRequest(request);

        when(tokenBlacklistService.isBlacklisted(VALID_TOKEN)).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(tokenBlacklistService).isBlacklisted(VALID_TOKEN);
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void positive_should_set_authentication_when_token_valid() throws Exception {

        addTokenToRequest(request);

        when(tokenBlacklistService.isBlacklisted(VALID_TOKEN)).thenReturn(false);
        when(jwtService.extractUserEmailFromAccessToken(VALID_TOKEN)).thenReturn(USER_EMAIL);
        when(userDetailsService.loadUserByUsername(USER_EMAIL)).thenReturn(userDetails);
        when(jwtService.validateAccessToken(VALID_TOKEN, userDetails)).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(USER_EMAIL,
                SecurityContextHolder.getContext().getAuthentication().getName());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void positive_should_not_override_existing_authentication() throws Exception {
        var existingAuth = new UsernamePasswordAuthenticationToken("already-authenticated", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        addTokenToRequest(request);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertEquals("already-authenticated", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(filterChain).doFilter(request, response);
    }
}
