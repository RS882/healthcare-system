package com.healthcare.auth_service.service;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.test.util.ReflectionTestUtils;

import static com.healthcare.auth_service.service.CookieService.REFRESH_TOKEN;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(value = DisplayNameGenerator.ReplaceUnderscores.class)
class CookieServiceTest {

    @InjectMocks
    private CookieService cookieService;

    private final long refreshExpiration = 1209600000L; // 14 дней
    private final String cookiePath = "/api/v1/auth";
    private final String tokenValue = "sample-refresh-token";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(cookieService, "refreshExpirationMs", refreshExpiration);
        ReflectionTestUtils.setField(cookieService, "path", cookiePath);
    }

    @Test
    void positive_should_set_refresh_token_to_cookie() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);

        cookieService.setRefreshTokenToCookie(response, tokenValue);

        verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), headerCaptor.capture());

        String cookie = headerCaptor.getValue();
        assertTrue(cookie.contains(REFRESH_TOKEN));
        assertTrue(cookie.contains(tokenValue));
        assertTrue(cookie.contains("HttpOnly"));
//        assertTrue(cookie.contains("Secure"));
//        assertTrue(cookie.contains("SameSite=None"));
        assertTrue(cookie.contains("Path=" + cookiePath));
    }

    @Test
    void should_remove_refresh_token_cookie() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);

        cookieService.removeRefreshTokenFromCookie(response);

        verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), headerCaptor.capture());

        String cookie = headerCaptor.getValue();
        assertTrue(cookie.contains(REFRESH_TOKEN));
        assertTrue(cookie.contains("Max-Age=0"));
        assertTrue(cookie.contains("Path=" + cookiePath));
    }
}
