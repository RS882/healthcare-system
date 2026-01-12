package com.healthcare.auth_service.service;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.healthcare.auth_service.service.RequestIdServiceImpl.REQUEST_ID_TTL;
import static com.healthcare.auth_service.service.RequestIdServiceImpl.REQUEST_ID_VALUE;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Request id service integration tests: ")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayNameGeneration(value = DisplayNameGenerator.ReplaceUnderscores.class)
class RequestIdServiceRedisIT {

    @Autowired
    private RequestIdServiceImpl requestIdService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void getRequestId_should_write_key_to_redis_with_value_and_ttl() {

        UUID id = requestIdService.getRequestId();

        String value = redisTemplate.opsForValue().get(id.toString());
        assertEquals(REQUEST_ID_VALUE, value);

        Long ttlMs = redisTemplate.getExpire(id.toString(), TimeUnit.MILLISECONDS);
        assertNotNull(ttlMs);
        assertTrue(ttlMs > 0);
        assertTrue(ttlMs <= REQUEST_ID_TTL);

        redisTemplate.delete(id.toString());
    }

    @Test
    void saveRequestId_should_return_true_and_store_key() {
        UUID id = UUID.randomUUID();

        boolean result = requestIdService.saveRequestId(id);

        assertTrue(result);

        String value = redisTemplate.opsForValue().get(id.toString());
        assertEquals(REQUEST_ID_VALUE, value);

        Long ttlMs = redisTemplate.getExpire(id.toString(), TimeUnit.MILLISECONDS);
        assertNotNull(ttlMs);
        assertTrue(ttlMs > 0);

        redisTemplate.delete(id.toString());
    }
}
