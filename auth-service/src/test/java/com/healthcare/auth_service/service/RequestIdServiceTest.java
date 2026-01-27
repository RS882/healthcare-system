package com.healthcare.auth_service.service;

import com.healthcare.auth_service.config.properties.RequestIdProperties;
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
    private RequestIdProperties props;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RequestIdServiceImpl service;

    private final String REDIS_KEY_PREFIX = "test-prefix";
    private final String REQUEST_ID_VALUE = "test_value";
    private final Duration REQUEST_ID_TTL = Duration.ofSeconds(30);

    @Nested
    @DisplayName("Get request id tests")
    public class GetRequestIdTests {

        @BeforeEach
        public void setUp() {
            when(props.prefix()).thenReturn(REDIS_KEY_PREFIX);
            when(props.value()).thenReturn(REQUEST_ID_VALUE);
            when(props.ttl()).thenReturn(REQUEST_ID_TTL);

            when(redis.opsForValue()).thenReturn(valueOperations);
        }

        @Test
        void positive_should_return_random_id() {

            when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                    .thenReturn(Boolean.TRUE);

            UUID result = service.getRequestId();

            assertNotNull(result);
            assertInstanceOf(UUID.class, result);

            verify(valueOperations).setIfAbsent(
                    eq(REDIS_KEY_PREFIX + result),
                    eq(REQUEST_ID_VALUE),
                    any(Duration.class)
            );
        }

        @Test
        void negative_should_throw_RequestIdSaveException_when_save_fails() {

            when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                    .thenReturn(Boolean.FALSE);

            assertThrows(RequestIdSaveException.class, () -> service.getRequestId());
        }

        @Test
        void negative_should_throw_RequestIdSaveException_when_redis_return_null() {

            when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                    .thenReturn(null);

            assertThrows(RequestIdSaveException.class, () -> service.getRequestId());
        }
    }

    @Nested
    @DisplayName("Save request id tests")
    public class SaveRequestIdTests {

        private UUID id;
        private String key;

        @BeforeEach
        public void setUp() {
            when(props.prefix()).thenReturn(REDIS_KEY_PREFIX);
            when(props.value()).thenReturn(REQUEST_ID_VALUE);
            when(props.ttl()).thenReturn(REQUEST_ID_TTL);

            when(redis.opsForValue()).thenReturn(valueOperations);

            id = UUID.randomUUID();
            key = REDIS_KEY_PREFIX + id;
        }

        @Test
        void positive_should_return_true() {

            when(valueOperations.setIfAbsent(
                    eq(key),
                    eq(REQUEST_ID_VALUE),
                    any()
            )).thenReturn(Boolean.TRUE);

            Boolean result = service.saveRequestId(id);

            assertTrue(result);

            verify(redis).opsForValue();

            verify(valueOperations).setIfAbsent(
                    eq(key),
                    eq(REQUEST_ID_VALUE),
                    eq(REQUEST_ID_TTL)
            );
        }

        @Test
        void negative_should_return_false() {

            when(valueOperations.setIfAbsent(
                    eq(key),
                    eq(REQUEST_ID_VALUE),
                    any()
            )).thenReturn(Boolean.FALSE);

            Boolean result = service.saveRequestId(id);

            assertFalse(result);

            verify(redis).opsForValue();

            verify(valueOperations).setIfAbsent(
                    eq(key),
                    eq(REQUEST_ID_VALUE),
                    eq(REQUEST_ID_TTL)
            );
        }

        @Test
        void negative_should_return_false_when_redis_return_null() {

            when(valueOperations.setIfAbsent(
                    eq(key),
                    eq(REQUEST_ID_VALUE),
                    any()
            )).thenReturn(null);

            Boolean result = service.saveRequestId(id);

            assertFalse(result);

            verify(redis).opsForValue();

            verify(valueOperations).setIfAbsent(
                    eq(key),
                    eq(REQUEST_ID_VALUE),
                    eq(REQUEST_ID_TTL)
            );
        }
    }

    @Nested
    @DisplayName("Is request id valid tests")
    public class IsRequestIdValidTests {

        private UUID id;
        private String key;

        @BeforeEach
        public void setUp() {
            when(props.prefix()).thenReturn(REDIS_KEY_PREFIX);

            id = UUID.randomUUID();
            key = REDIS_KEY_PREFIX + id;
        }

        @Test
        void positive_should_return_true() {

            when(redis.hasKey(key)).thenReturn(Boolean.TRUE);

            Boolean result = service.isRequestIdValid(id.toString());

            assertTrue(result);

            verify(redis).hasKey(key);
        }

        @Test
        void negative_should_return_false() {

            when(redis.hasKey(key)).thenReturn(Boolean.FALSE);

            Boolean result = service.isRequestIdValid(id.toString());

            assertFalse(result);

            verify(redis).hasKey(key);
        }

        @Test
        void negative_should_return_false_when_redis_return_null() {

            when(redis.hasKey(key)).thenReturn(null);

            Boolean result = service.isRequestIdValid(id.toString());

            assertFalse(result);

            verify(redis).hasKey(key);
        }
    }
}