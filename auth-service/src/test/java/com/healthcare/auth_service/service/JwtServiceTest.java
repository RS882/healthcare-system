package com.healthcare.auth_service.service;

import com.healthcare.auth_service.config.properties.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(value = DisplayNameGenerator.ReplaceUnderscores.class)
class JwtServiceTest {

    @Mock
    private  JwtProperties jwtProps;

    @InjectMocks
    private JwtService jwtService;

    private final String accessSecret = Base64.getEncoder().encodeToString("access-test-secret-key-which-is-long".getBytes());
    private final String refreshSecret = Base64.getEncoder().encodeToString("refresh-test-secret-key-which-is-long".getBytes());

    private final String USER_EMAIL = "test@example.com";
    private final String USER_PASSWORD = "password";
    private final String USER_ROLE = "ROLE_TEST";
    private final Long USER_ID = 1L;
    private final String INVALID_TOKEN = "invalid.token.value";
    private Duration ACCESS_EXPIRATION = Duration.ofMinutes(15);
    private Duration REFRESH_EXPIRATION = Duration.ofDays(30);

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {

        when(jwtProps.accessSecret()).thenReturn(accessSecret);
        when(jwtProps.refreshSecret()).thenReturn(refreshSecret);

        jwtService.initKey();

        userDetails = User.withUsername(USER_EMAIL)
                .password(USER_PASSWORD)
                .authorities(USER_ROLE)
                .build();
    }

    @Test
    void positive_test_generate_and_validate_access_token() {

        when(jwtProps.accessTokenExpiration()).thenReturn(ACCESS_EXPIRATION);
        when(jwtProps.refreshTokenExpiration()).thenReturn(REFRESH_EXPIRATION);

        String token = jwtService.getTokens(userDetails, USER_ID).getAccessToken();

        assertNotNull(token);
        assertTrue(jwtService.validateAccessToken(token, userDetails));
        assertEquals(USER_EMAIL, jwtService.extractUserEmailFromAccessToken(token));
        assertEquals(USER_ID, jwtService.extractUserIdFromAccessToken(token));
        assertEquals(List.of(USER_ROLE), jwtService.extractRolesFromAccessToken(token));
        assertTrue(jwtService.getRemainingTTLAccessToken(token) > 0);
    }

    @Test
    void positive_test_generate_and_validate_refresh_token() {

        when(jwtProps.accessTokenExpiration()).thenReturn(ACCESS_EXPIRATION);
        when(jwtProps.refreshTokenExpiration()).thenReturn(REFRESH_EXPIRATION);

        String token = jwtService.getTokens(userDetails, USER_ID).getRefreshToken();

        assertNotNull(token);
        assertTrue(jwtService.validateRefreshToken(token, userDetails));
        assertEquals(USER_EMAIL, jwtService.extractUserEmailFromRefreshToken(token));
        assertTrue(jwtService.getRemainingTTLRefreshToken(token) > 0);
    }

    @Test
    void negative_test_invalid_access_token() {
        assertFalse(jwtService.validateAccessToken(INVALID_TOKEN, userDetails));
    }

    @Test
    void negative_test_invalid_refresh_token() {
        assertFalse(jwtService.validateRefreshToken(INVALID_TOKEN, userDetails));
    }
}