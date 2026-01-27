package com.healthcare.api_gateway.filter;


import com.healthcare.api_gateway.config.properties.RequestIdProperties;
import com.healthcare.api_gateway.service.RequestIdReactiveServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Request ID service tests")
class RequestIdReactiveServiceImplTest {

    @Mock
    private ReactiveStringRedisTemplate redis;

    @Mock
    private RequestIdProperties props;

    @Mock
    private ReactiveValueOperations<String, String> valueOps;

    @InjectMocks
    private RequestIdReactiveServiceImpl service;

    private static final String PREFIX = "request-id:";
    private static final String VALUE = "test-gateway";
    private static final Duration TTL = Duration.ofSeconds(30);

    private void stubRequestIdProperties() {
        stubPrefixProperties();
        when(props.value()).thenReturn(VALUE);
        when(props.ttl()).thenReturn(TTL);
    }

    private void stubPrefixProperties() {
        when(props.prefix()).thenReturn(PREFIX);
    }

    @Nested
    @DisplayName("Is UUID valid tests")
    class IsValidUuidTests {

        @Test
        void should_return_false_for_null_or_blank() {
            Assertions.assertFalse(service.isValidUuid(null));
            Assertions.assertFalse(service.isValidUuid(""));
            Assertions.assertFalse(service.isValidUuid("   "));
        }

        @Test
        void should_return_false_for_invalid_uuid() {
            Assertions.assertFalse(service.isValidUuid("not-a-uuid"));
        }

        @Test
        void should_return_true_for_valid_uuid() {
            Assertions.assertTrue(service.isValidUuid(UUID.randomUUID().toString()));
        }
    }

    @Nested
    @DisplayName("Resolve or generate tests")
    class ResolveOrGenerateTests {

        @Test
        void should_return_existing_header_when_valid_uuid() {
            String header = UUID.randomUUID().toString();

            String result = service.resolveOrGenerate(header);

            Assertions.assertEquals(header, result);
        }

        @Test
        void should_generate_new_uuid_when_header_null_blank_or_invalid() {
            String r1 = service.resolveOrGenerate(null);
            String r2 = service.resolveOrGenerate("   ");
            String r3 = service.resolveOrGenerate("not-a-uuid");

            Assertions.assertTrue(service.isValidUuid(r1));
            Assertions.assertTrue(service.isValidUuid(r2));
            Assertions.assertTrue(service.isValidUuid(r3));
        }
    }

    @Nested
    @DisplayName("Save tests")
    class SaveTests {

        @Test
        void should_return_false_and_not_call_redis_when_invalid_uuid() {
            StepVerifier.create(service.save("not-a-uuid"))
                    .expectNext(false)
                    .verifyComplete();

            verifyNoInteractions(redis);
            verifyNoInteractions(valueOps);
        }

        @Test
        void should_save_to_redis_and_return_true_when_setIfAbsent_true() {

            stubRequestIdProperties();

            String id = UUID.randomUUID().toString();
            String key = PREFIX + id;

            when(redis.opsForValue()).thenReturn(valueOps);
            when(valueOps.setIfAbsent(key, VALUE, TTL)).thenReturn(Mono.just(true));

            StepVerifier.create(service.save(id))
                    .expectNext(true)
                    .verifyComplete();

            verify(redis).opsForValue();
            verify(valueOps).setIfAbsent(key, VALUE, TTL);
        }

        @Test
        void should_return_false_when_redis_errors() {

            stubRequestIdProperties();

            String id = UUID.randomUUID().toString();
            String key = PREFIX + id;

            when(redis.opsForValue()).thenReturn(valueOps);
            when(valueOps.setIfAbsent(key, VALUE, TTL)).thenReturn(Mono.error(new RuntimeException("redis down")));

            StepVerifier.create(service.save(id))
                    .expectNext(false)
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("Exists tests")
    class ExistsTests {

        @Test
        void should_return_false_and_not_call_redis_when_invalid_uuid() {
            StepVerifier.create(service.exists("bad"))
                    .expectNext(false)
                    .verifyComplete();

            verifyNoInteractions(redis);
        }

        @Test
        void should_call_hasKey_and_return_value() {
            stubPrefixProperties();

            String id = UUID.randomUUID().toString();
            String key = PREFIX + id;

            when(redis.hasKey(key)).thenReturn(Mono.just(true));

            StepVerifier.create(service.exists(id))
                    .expectNext(true)
                    .verifyComplete();

            verify(redis).hasKey(key);
        }

        @Test
        void should_return_false_when_redis_errors() {
            stubPrefixProperties();

            String id = UUID.randomUUID().toString();
            String key = PREFIX + id;

            when(redis.hasKey(key)).thenReturn(Mono.error(new RuntimeException("redis down")));

            StepVerifier.create(service.exists(id))
                    .expectNext(false)
                    .verifyComplete();

            verify(redis).hasKey(key);
        }
    }

    @Nested
    @DisplayName("TTL tests")
    class TtlTests {

        @Test
        void should_return_zero_and_not_call_redis_when_invalid_uuid() {
            StepVerifier.create(service.ttl("bad"))
                    .expectNext(Duration.ZERO)
                    .verifyComplete();

            verifyNoInteractions(redis);
        }

        @Test
        void should_return_duration_when_positive() {
            stubPrefixProperties();

            String id = UUID.randomUUID().toString();
            String key = PREFIX + id;
            Duration d = Duration.ofSeconds(12);

            when(redis.getExpire(key)).thenReturn(Mono.just(d));

            StepVerifier.create(service.ttl(id))
                    .expectNext(d)
                    .verifyComplete();

            verify(redis).getExpire(key);
        }

        @Test
        void should_return_zero_when_null() {
            stubPrefixProperties();

            String id = UUID.randomUUID().toString();
            String key = PREFIX + id;

            when(redis.getExpire(key)).thenReturn(Mono.justOrEmpty((Duration) null));

            StepVerifier.create(service.ttl(id))
                    .expectNext(Duration.ZERO)
                    .verifyComplete();
        }

        @Test
        void should_return_zero_when_negative() {
            stubPrefixProperties();

            String id = UUID.randomUUID().toString();
            String key = PREFIX + id;

            when(redis.getExpire(key)).thenReturn(Mono.just(Duration.ofSeconds(-1)));

            StepVerifier.create(service.ttl(id))
                    .expectNext(Duration.ZERO)
                    .verifyComplete();
        }

        @Test
        void should_return_zero_when_redis_errors() {
            stubPrefixProperties();

            String id = UUID.randomUUID().toString();
            String key = PREFIX + id;

            when(redis.getExpire(key)).thenReturn(Mono.error(new RuntimeException("redis down")));

            StepVerifier.create(service.ttl(id))
                    .expectNext(Duration.ZERO)
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("To redis key tests")
    class ToRedisKeyTests {

        @BeforeEach
        void setUp() {
            stubPrefixProperties();
        }

        @Test
        void should_prefix_request_id() {
            String id = UUID.randomUUID().toString();
            Assertions.assertEquals(PREFIX + id, service.toRedisKey(id));
        }
    }
}
