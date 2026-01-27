package com.healthcare.auth_service.service;

import com.healthcare.auth_service.config.properties.JwtProperties;
import com.healthcare.auth_service.config.properties.PrefixProperties;
import com.healthcare.auth_service.exception_handler.exception.AccessDeniedException;
import com.healthcare.auth_service.service.interfacies.BlockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(value = DisplayNameGenerator.ReplaceUnderscores.class)
class RefreshTokenServiceTest {

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    @Mock
    private StringRedisTemplate redis;

    @Mock
    private SetOperations<String, String> setOps;

    @Mock
    private ValueOperations<String, String> valueOps;

    @Mock
    private BlockService blockService;

    @Mock
    private JwtProperties jwtProps;

    @Mock
    private PrefixProperties prefixProps;

    private final Long USER_ID = 1L;
    private final String TOKEN = "sample-refresh-token";

    private final Duration REFRESH_EXPIRATION = Duration.ofSeconds(1);
    private final int MAX_TOKENS = 5;
    private final String REFRESH_PREFIX = "refresh:";

    private String prefix;
    private String setPrefix;

    @BeforeEach
    void setUp() {

        setPrefix = REFRESH_PREFIX + USER_ID;
        prefix = setPrefix + ":";

        lenient().when(redis.opsForSet()).thenReturn(setOps);
        lenient().when(redis.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void positive_should_save_token_successfully() {

        when(jwtProps.refreshTokenExpiration()).thenReturn(REFRESH_EXPIRATION);
        when(jwtProps.maxTokens()).thenReturn(MAX_TOKENS);
        when(prefixProps.refresh()).thenReturn(REFRESH_PREFIX);

        when(blockService.isBlocked(USER_ID)).thenReturn(false);
        when(setOps.size(setPrefix)).thenReturn(3L);

        assertDoesNotThrow(() -> refreshTokenService.save(TOKEN, USER_ID));

        verify(setOps).add(setPrefix, TOKEN);
        verify(redis).expire(setPrefix, REFRESH_EXPIRATION);
        verify(valueOps).set(prefix + TOKEN, "valid", REFRESH_EXPIRATION);
    }

    @Test
    void negative_should_throw_if_blocked() {
        when(blockService.isBlocked(USER_ID)).thenReturn(true);

        assertThrows(AccessDeniedException.class, () -> refreshTokenService.save(TOKEN, USER_ID));
    }

    @Test
    void negative_should_block_and_throw_if_too_many_tokens() {

        when(jwtProps.maxTokens()).thenReturn(MAX_TOKENS);
        when(prefixProps.refresh()).thenReturn(REFRESH_PREFIX);

        when(blockService.isBlocked(USER_ID)).thenReturn(false);
        when(setOps.size(setPrefix)).thenReturn(10L);

        assertThrows(AccessDeniedException.class, () -> refreshTokenService.save(TOKEN, USER_ID));
        verify(blockService).block(USER_ID);
    }

    @Test
    void positive_should_check_token_validity() {

        when(prefixProps.refresh()).thenReturn(REFRESH_PREFIX);

        when(redis.hasKey(prefix + TOKEN)).thenReturn(true);
        assertTrue(refreshTokenService.isValid(TOKEN, USER_ID));
    }

    @Test
    void negative_should_return_false_if_token_invalid() {

        when(prefixProps.refresh()).thenReturn(REFRESH_PREFIX);

        when(redis.hasKey(prefix + TOKEN)).thenReturn(false);
        assertFalse(refreshTokenService.isValid(TOKEN, USER_ID));
    }

    @Test
    void positive_should_delete_token() {

        when(prefixProps.refresh()).thenReturn(REFRESH_PREFIX);

        refreshTokenService.delete(TOKEN, USER_ID);

        verify(setOps).remove(setPrefix, TOKEN);
        verify(redis).delete(prefix + TOKEN);
    }

    @Test
    void positive_should_delete_all_tokens() {

        when(prefixProps.refresh()).thenReturn(REFRESH_PREFIX);

        Set<String> tokens = Set.of("a", "b", "c");
        when(setOps.members(setPrefix)).thenReturn(tokens);

        refreshTokenService.deleteAll(USER_ID);

        verify(redis).delete(setPrefix);
        for (String token : tokens) {
            verify(redis).delete(prefix + token);
        }
    }
}
