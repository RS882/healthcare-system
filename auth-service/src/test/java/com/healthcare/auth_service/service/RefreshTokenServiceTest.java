package com.healthcare.auth_service.service;

import com.healthcare.auth_service.exception_handler.exception.AccessDeniedException;
import com.healthcare.auth_service.service.interfacies.BlockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.junit.jupiter.MockitoExtension;

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

    private final Long USER_ID = 1L;
    private final String TOKEN = "sample-refresh-token";

    private final long refreshExpiration = 1000L;
    private final int maxTokens = 5;
    private final String refreshPrefix = "refresh:";

    private String prefix;
    private String setPrefix;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshExpirationMs", refreshExpiration);
        ReflectionTestUtils.setField(refreshTokenService, "maxTokens", maxTokens);
        ReflectionTestUtils.setField(refreshTokenService, "refreshPrefix", refreshPrefix);

        setPrefix = refreshPrefix + USER_ID;
        prefix = setPrefix + ":";

        lenient().when(redis.opsForSet()).thenReturn(setOps);
        lenient().when(redis.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void positive_should_save_token_successfully() {

        when(blockService.isBlocked(USER_ID)).thenReturn(false);
        when(setOps.size(setPrefix)).thenReturn(3L);

        assertDoesNotThrow(() -> refreshTokenService.save(TOKEN, USER_ID));

        verify(setOps).add(setPrefix, TOKEN);
        verify(redis).expire(setPrefix, Duration.ofMillis(refreshExpiration));
        verify(valueOps).set(prefix + TOKEN, "valid", Duration.ofMillis(refreshExpiration));
    }

    @Test
    void negative_should_throw_if_blocked() {
        when(blockService.isBlocked(USER_ID)).thenReturn(true);

        assertThrows(AccessDeniedException.class, () -> refreshTokenService.save(TOKEN, USER_ID));
    }

    @Test
    void negative_should_block_and_throw_if_too_many_tokens() {
        when(blockService.isBlocked(USER_ID)).thenReturn(false);
        when(setOps.size(setPrefix)).thenReturn(10L);

        assertThrows(AccessDeniedException.class, () -> refreshTokenService.save(TOKEN, USER_ID));
        verify(blockService).block(USER_ID);
    }

    @Test
    void positive_should_check_token_validity() {
        when(redis.hasKey(prefix+ TOKEN)).thenReturn(true);
        assertTrue(refreshTokenService.isValid(TOKEN, USER_ID));
    }

    @Test
    void negative_should_return_false_if_token_invalid() {
        when(redis.hasKey(prefix + TOKEN)).thenReturn(false);
        assertFalse(refreshTokenService.isValid(TOKEN, USER_ID));
    }

    @Test
    void positive_should_delete_token() {
        refreshTokenService.delete(TOKEN, USER_ID);

        verify(setOps).remove(setPrefix, TOKEN);
        verify(redis).delete(prefix + TOKEN);
    }

    @Test
    void positive_should_delete_all_tokens() {
        Set<String> tokens = Set.of("a", "b", "c");
        when(setOps.members(setPrefix)).thenReturn(tokens);

        refreshTokenService.deleteAll(USER_ID);

        verify(redis).delete(setPrefix);
        for (String token : tokens) {
            verify(redis).delete(prefix + token);
        }
    }
}
