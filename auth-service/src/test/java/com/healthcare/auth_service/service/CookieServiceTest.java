package com.healthcare.auth_service.service;

import com.healthcare.auth_service.config.properties.JwtProperties;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import java.time.Duration;

import static com.healthcare.auth_service.service.constant.RefreshTokenTitle.REFRESH_TOKEN;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(value = DisplayNameGenerator.ReplaceUnderscores.class)
class CookieServiceTest {

    @Mock
    private JwtProperties jwtProps;

    @InjectMocks
    private CookieService cookieService;

    private final Duration REFRESH_EXPIRATION = Duration.ofDays(14); // 14 days
    private final String COOKIE_PATH = "/api/v1/auth";
    private final String TOKEN_VALUE = "sample-refresh-token";

    @Test
    void positive_should_set_refresh_token_to_cookie() {

        when(jwtProps.refreshTokenExpiration()).thenReturn(REFRESH_EXPIRATION);
        when(jwtProps.cookiePath()).thenReturn(COOKIE_PATH);

        HttpServletResponse response = mock(HttpServletResponse.class);
        ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);

        cookieService.setRefreshTokenToCookie(response, TOKEN_VALUE);

        verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), headerCaptor.capture());

        String cookie = headerCaptor.getValue();
        assertTrue(cookie.contains(REFRESH_TOKEN));
        assertTrue(cookie.contains(TOKEN_VALUE));
        assertTrue(cookie.contains("HttpOnly"));
        assertTrue(cookie.contains("Path=" + COOKIE_PATH));
    }

    @Test
    void should_remove_refresh_token_cookie() {

        when(jwtProps.cookiePath()).thenReturn(COOKIE_PATH);

        HttpServletResponse response = mock(HttpServletResponse.class);
        ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);

        cookieService.removeRefreshTokenFromCookie(response);

        verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), headerCaptor.capture());

        String cookie = headerCaptor.getValue();
        assertTrue(cookie.contains(REFRESH_TOKEN));
        assertTrue(cookie.contains("Max-Age=0"));
        assertTrue(cookie.contains("Path=" + COOKIE_PATH));
    }
}
