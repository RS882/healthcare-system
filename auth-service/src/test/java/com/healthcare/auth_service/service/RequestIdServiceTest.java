package com.healthcare.auth_service.service;

import com.healthcare.auth_service.exception_handler.exception.RequestIdSaveException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.UUID;

import static com.healthcare.auth_service.service.RequestIdServiceImpl.REQUEST_ID_TTL;
import static com.healthcare.auth_service.service.RequestIdServiceImpl.REQUEST_ID_VALUE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(value = DisplayNameGenerator.ReplaceUnderscores.class)
class RequestIdServiceTest {

    @Mock
    private StringRedisTemplate redis;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RequestIdServiceImpl service;

    @Nested
    @DisplayName("Get request id tests")
    public class GetRequestIdTests {

        @Test
        void positive_should_return_random_id() {

            when(redis.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                    .thenReturn(true);

            UUID result = service.getRequestId();

            assertNotNull(result);
            assertInstanceOf(UUID.class, result);

            verify(valueOperations).setIfAbsent(
                    eq(result.toString()),
                    eq(REQUEST_ID_VALUE),
                    any(Duration.class)
            );
        }

        @Test
        void negative_should_throw_RequestIdSaveException_when_save_fails() {

            when(redis.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                    .thenReturn(false);

            assertThrows(RequestIdSaveException.class, () -> service.getRequestId());
        }
    }

    @Test
    void positive_should_return_true() {

        UUID id = UUID.randomUUID();

        when(redis.opsForValue()).thenReturn(valueOperations);

        when(valueOperations.setIfAbsent(
                eq(id.toString()),
                eq(REQUEST_ID_VALUE),
                any()
        )).thenReturn(true);

        boolean result = service.saveRequestId(id);

        assertTrue(result);
        assertTrue(result);

        verify(redis).opsForValue();

        verify(valueOperations).setIfAbsent(
                eq(id.toString()),
                eq(REQUEST_ID_VALUE),
                eq(Duration.ofMillis(REQUEST_ID_TTL))
        );
    }
}