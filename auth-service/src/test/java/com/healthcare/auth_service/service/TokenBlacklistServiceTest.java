package com.healthcare.auth_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(value = DisplayNameGenerator.ReplaceUnderscores.class)
class TokenBlacklistServiceTest {

    @InjectMocks
    private TokenBlacklistServiceImpl blacklistService;

    @Mock
    private StringRedisTemplate redis;

    @Mock
    private ValueOperations<String, String> valueOps;

    private final String PREFIX = "blacklist:";
    private final String TOKEN = "sample-access-token";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(blacklistService, "blacklistPrefix", PREFIX);
        lenient().when(redis.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void positive_should_blacklist_token_when_valid() {
        long ttl = 1000L;

        assertDoesNotThrow(() -> blacklistService.blacklist(TOKEN, ttl));

        verify(valueOps).set(PREFIX + TOKEN, "blacklisted", Duration.ofMillis(ttl));
    }

    @Test
    void negative_should_not_blacklist_token_when_invalid_token() {
        blacklistService.blacklist("", 1000L);
        verify(valueOps, never()).set(anyString(), anyString(), any());
    }

    @Test
    void negative_should_not_blacklist_token_when_ttl_invalid() {
        blacklistService.blacklist(TOKEN, 0L);
        verify(valueOps, never()).set(anyString(), anyString(), any());
    }

    @Test
    void positive_should_return_true_if_token_blacklisted() {
        when(redis.hasKey(PREFIX + TOKEN)).thenReturn(true);
        assertTrue(blacklistService.isBlacklisted(TOKEN));
    }

    @Test
    void negative_should_return_false_if_token_not_blacklisted() {
        when(redis.hasKey(PREFIX + TOKEN)).thenReturn(false);
        assertFalse(blacklistService.isBlacklisted(TOKEN));
    }
}
