package com.healthcare.auth_service.service;

import com.healthcare.auth_service.domain.AuthUserDetails;
import com.healthcare.auth_service.domain.dto.LoginDto;
import com.healthcare.auth_service.domain.dto.RegistrationDto;
import com.healthcare.auth_service.domain.dto.TokensDto;
import com.healthcare.auth_service.exception_handler.exception.AccessDeniedException;
import com.healthcare.auth_service.exception_handler.exception.UnauthorizedException;
import com.healthcare.auth_service.service.interfacies.BlockService;
import com.healthcare.auth_service.service.interfacies.RefreshTokenService;
import com.healthcare.auth_service.service.interfacies.TokenBlacklistService;
import com.healthcare.auth_service.service.interfacies.UserClientService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(value = DisplayNameGenerator.ReplaceUnderscores.class)
class AuthServiceTest {

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private UserClientService userClientService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authManager;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private BlockService blockService;
    @Mock
    private TokenBlacklistService tokenBlacklistService;

    private final String EMAIL = "test@example.com";
    private final String PASSWORD = "password";
    private final Long USER_ID = 1L;
    private final String USER_ROLE = "ROLE_TEST";
    private final String ACCESS_TOKEN = "access_token";
    private final String REFRESH_TOKEN = "refresh_token";

    private AuthUserDetails userDetails;
    private TokensDto tokens;

    @BeforeEach
    void setup() {

        userDetails = new AuthUserDetails(
                USER_ID,
                EMAIL,
                PASSWORD,
                List.of(new SimpleGrantedAuthority(USER_ROLE)),
                true
        );

        tokens = new TokensDto(ACCESS_TOKEN, REFRESH_TOKEN);
    }

    @Nested
    @DisplayName("User registration tests")
    public class RegistrationTests {

        @Test
        void positive_should_return_tokens_and_save_refresh_token() {
            RegistrationDto dto = RegistrationDto.builder()
                    .userName("test")
                    .password(PASSWORD)
                    .userEmail(EMAIL)
                    .build();

            when(passwordEncoder.encode(PASSWORD)).thenReturn("encodedPass");
            when(userClientService.registerUser(any())).thenReturn(userDetails);
            when(jwtService.getTokens(eq(userDetails), eq(USER_ID))).thenReturn(tokens);

            TokensDto result = authService.registration(dto);

            assertEquals(tokens, result);
            verify(refreshTokenService).save(REFRESH_TOKEN, USER_ID);
        }
    }

    @Nested
    @DisplayName("User login tests")
    public class LoginTests {

        @Test
        void positive_should_return_tokens_when_user_not_blocked() {

            LoginDto dto = new LoginDto(EMAIL, PASSWORD);

            when(userClientService.getUserByEmail(EMAIL)).thenReturn(userDetails);
            when(blockService.isBlocked(USER_ID)).thenReturn(false);
            when(jwtService.getTokens(eq(userDetails), eq(USER_ID))).thenReturn(tokens);

            TokensDto result = authService.login(dto);

            assertEquals(tokens, result);
            verify(authManager).authenticate(any());
            verify(refreshTokenService).save(REFRESH_TOKEN, USER_ID);
        }

        @Test
        void negative_should_throw_if_user_blocked() {

            LoginDto dto = new LoginDto(EMAIL, PASSWORD);

            when(userClientService.getUserByEmail(EMAIL)).thenReturn(userDetails);
            when(blockService.isBlocked(USER_ID)).thenReturn(true);

            assertThrows(AccessDeniedException.class, () -> authService.login(dto));
        }
    }

    @Nested
    @DisplayName("Refresh tokens tests")
    public class RefreshTests {

        @Test
        void positive_should_validate_token_and_return_new_tokens() {
            String refresh = "old" + REFRESH_TOKEN;

            when(jwtService.extractUserEmailFromRefreshToken(refresh)).thenReturn(EMAIL);
            when(userClientService.getUserByEmail(EMAIL)).thenReturn(userDetails);
            when(jwtService.validateRefreshToken(refresh, userDetails)).thenReturn(true);
            when(refreshTokenService.isValid(refresh, USER_ID)).thenReturn(true);
            when(jwtService.getTokens(userDetails, USER_ID)).thenReturn(tokens);

            TokensDto result = authService.refresh(refresh);

            assertEquals(tokens, result);
            verify(refreshTokenService).delete(refresh, USER_ID);
            verify(refreshTokenService).save(REFRESH_TOKEN, USER_ID);
        }

        @Test
        void negative_should_throw_if_token_is_invalid() {
            String refresh = "invalid-token";
            when(jwtService.extractUserEmailFromRefreshToken(refresh)).thenThrow(new RuntimeException("error"));

            assertThrows(UnauthorizedException.class, () -> authService.refresh(refresh));
        }

        @Test
        void negative_should_throw_if_token_is_null() {

            assertThrows(UnauthorizedException.class, () -> authService.refresh(null));
        }
    }

    @Nested
    @DisplayName("Logout tests")
    public class LogoutTests {
        @Test
        void positive_should_blacklist_access_token_and_delete_refresh_token() {

            when(jwtService.getRemainingTTLAccessToken(ACCESS_TOKEN)).thenReturn(10000L);
            when(jwtService.extractUserEmailFromRefreshToken(REFRESH_TOKEN)).thenReturn(EMAIL);
            when(userClientService.getUserByEmail(EMAIL)).thenReturn(userDetails);
            when(jwtService.validateRefreshToken(REFRESH_TOKEN, userDetails)).thenReturn(true);
            when(refreshTokenService.isValid(REFRESH_TOKEN, USER_ID)).thenReturn(true);

            authService.logout(REFRESH_TOKEN, ACCESS_TOKEN);

            verify(tokenBlacklistService).blacklist(ACCESS_TOKEN, 10000L);
            verify(refreshTokenService).delete(REFRESH_TOKEN, USER_ID);
            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }

        @Test
        void negative_should_only_blacklist_if_refresh_token_is_blank() {
            authService.logout("", ACCESS_TOKEN);

            verify(tokenBlacklistService).blacklist(eq(ACCESS_TOKEN), anyLong());
            verify(refreshTokenService, never()).delete(any(), any());
        }

        @Test
        void negative_should_only_delete_refresh_if_access_token_is_blank() {

            when(jwtService.extractUserEmailFromRefreshToken(REFRESH_TOKEN)).thenReturn(EMAIL);
            when(userClientService.getUserByEmail(EMAIL)).thenReturn(userDetails);
            when(jwtService.validateRefreshToken(REFRESH_TOKEN, userDetails)).thenReturn(true);
            when(refreshTokenService.isValid(REFRESH_TOKEN, USER_ID)).thenReturn(true);

            authService.logout(REFRESH_TOKEN, "");

            verify(refreshTokenService).delete(REFRESH_TOKEN, USER_ID);
            verify(tokenBlacklistService, never()).blacklist(any(), anyLong());
        }
    }
}
