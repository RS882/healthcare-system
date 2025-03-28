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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(value = DisplayNameGenerator.ReplaceUnderscores.class)
class BlockServiceTest {

    @InjectMocks
    private BlockServiceImpl blockService;

    @Mock
    private StringRedisTemplate redis;

    @Mock
    private ValueOperations<String, String> valueOps;

    private final long expiration = 60000L; // 1 минута
    private final String prefix = "refresh-block:";
    private final Long userId = 42L;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(blockService, "accessExpirationMs", expiration);
        ReflectionTestUtils.setField(blockService, "blockedPrefix", prefix);
        lenient().when(redis.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void positive_should_block_user() {
        blockService.block(userId);

        verify(valueOps).set(
                prefix + userId,
                "blocked",
                Duration.ofMillis(expiration)
        );
    }

    @Test
    void positive_should_return_true_if_user_blocked() {
        when(redis.hasKey(prefix + userId)).thenReturn(true);
        assertTrue(blockService.isBlocked(userId));
    }

    @Test
    void negative_should_return_false_if_user_not_blocked() {
        when(redis.hasKey(prefix + userId)).thenReturn(false);
        assertFalse(blockService.isBlocked(userId));
    }
}
