package com.healthcare.auth_service.service;

import com.healthcare.auth_service.config.properties.JwtProperties;
import com.healthcare.auth_service.config.properties.PrefixProperties;
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

    @Mock
    private  JwtProperties jwtProps;

    @Mock
    private  PrefixProperties prefixProps;

    private final Duration ACCESS_EXPIRATION = Duration.ofMinutes(1);
    private final String BLOCK_PREFIX = "refresh-block:";
    private final Long USER_ID = 42L;

    @BeforeEach
    void setUp() {
        lenient().when(redis.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void positive_should_block_user() {

        when(jwtProps.accessTokenExpiration()).thenReturn(ACCESS_EXPIRATION);
        when(prefixProps.blocked()).thenReturn(BLOCK_PREFIX);

        blockService.block(USER_ID);

        verify(valueOps).set(
                BLOCK_PREFIX + USER_ID,
                "blocked",
                ACCESS_EXPIRATION
        );
    }

    @Test
    void positive_should_return_true_if_user_blocked() {

        when(prefixProps.blocked()).thenReturn(BLOCK_PREFIX);
        when(redis.hasKey(BLOCK_PREFIX + USER_ID)).thenReturn(true);

        assertTrue(blockService.isBlocked(USER_ID));
    }

    @Test
    void negative_should_return_false_if_user_not_blocked() {

        when(prefixProps.blocked()).thenReturn(BLOCK_PREFIX);
        when(redis.hasKey(BLOCK_PREFIX + USER_ID)).thenReturn(false);

        assertFalse(blockService.isBlocked(USER_ID));
    }
}
