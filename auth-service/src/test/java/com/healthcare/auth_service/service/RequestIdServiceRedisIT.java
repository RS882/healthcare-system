package com.healthcare.auth_service.service;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.healthcare.auth_service.service.RequestIdServiceImpl.*;
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

        String redisKey = REDIS_KEY_PREFIX + id;

        String value = redisTemplate.opsForValue().get(redisKey);
        assertEquals(REQUEST_ID_VALUE, value);

        Long ttlMs = redisTemplate.getExpire(redisKey, TimeUnit.MILLISECONDS);
        assertNotNull(ttlMs);
        assertTrue(ttlMs > 0);
        assertTrue(ttlMs <= REQUEST_ID_TTL);

        redisTemplate.delete(redisKey);
    }

    @Test
    void saveRequestId_should_return_true_and_store_key() {
        UUID id = UUID.randomUUID();

        String redisKey = REDIS_KEY_PREFIX + id;

        boolean result = requestIdService.saveRequestId(id);

        assertTrue(result);

        String value = redisTemplate.opsForValue().get(redisKey);
        assertEquals(REQUEST_ID_VALUE, value);

        Long ttlMs = redisTemplate.getExpire(redisKey, TimeUnit.MILLISECONDS);
        assertNotNull(ttlMs);
        assertTrue(ttlMs > 0);

        redisTemplate.delete(redisKey);
    }

    @Test
    void request_ID_is_valid() {

        UUID id = requestIdService.getRequestId();

        boolean result = requestIdService.isRequestIdValid(id.toString());

        assertTrue(result);
    }

    @Test
    void request_ID_isnt_valid_when_id_is_null() {

        boolean result = requestIdService.isRequestIdValid(null);

        assertFalse(result);
    }

    @Test
    void request_ID_isnt_valid_when_id_is_blank() {

        boolean result = requestIdService.isRequestIdValid("  ");

        assertFalse(result);
    }

    @Test
    void request_ID_isnt_valid_when_id_is_empty() {

        boolean result = requestIdService.isRequestIdValid("");

        assertFalse(result);
    }

    @Test
    void request_ID_isnt_valid_when_id_is_incorrect() {

        boolean result = requestIdService.isRequestIdValid("asfsadgergh     35162yer5yaZZ_+--");

        assertFalse(result);
    }

    @Test
    void request_ID_isnt_valid_when_id_is_erroneous() {

        UUID id = requestIdService.getRequestId();

        boolean result = requestIdService.isRequestIdValid(UUID.randomUUID().toString());

        assertFalse(result);
    }
}
