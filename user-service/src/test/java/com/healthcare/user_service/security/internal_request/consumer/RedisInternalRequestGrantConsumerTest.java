package com.healthcare.user_service.security.internal_request.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.user_service.exception_handler.exception.InternalRequestAuthenticationServiceException;
import com.healthcare.user_service.exception_handler.exception.InternalRequestGrantInvalidException;
import com.healthcare.user_service.exception_handler.exception.InternalRequestGrantNotFoundException;
import com.healthcare.user_service.security.internal_request.constant.InternalService;
import com.healthcare.user_service.security.internal_request.dto.InternalRequestGrant;
import com.healthcare.user_service.security.internal_request.properties.InternalRequestConsumerProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Redis internal request grant consumer tests")
class RedisInternalRequestGrantConsumerTest {

    private static final String KEY_PREFIX =
            "internal-request:";

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private InternalRequestConsumerProperties props;

    @InjectMocks
    private RedisInternalRequestGrantConsumer consumer;

    @Test
    void should_consume_and_return_internal_request_grant()
            throws Exception {

        UUID internalRequestId =
                UUID.randomUUID();

        String redisKey =
                KEY_PREFIX + internalRequestId;

        String serializedGrant =
                """
                {
                  "issuer": "AUTH_SERVICE",
                  "target": "user-service",
                  "method": "POST",
                  "path": "/v1/users/internal/lookup"
                }
                """;

        InternalRequestGrant grant =
                validGrant();

        when(props.keyPrefix())
                .thenReturn(KEY_PREFIX);

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.getAndDelete(redisKey))
                .thenReturn(serializedGrant);

        when(objectMapper.readValue(
                serializedGrant,
                InternalRequestGrant.class
        )).thenReturn(grant);

        InternalRequestGrant result =
                consumer.consume(internalRequestId);

        assertThat(result)
                .isSameAs(grant);

        verify(valueOperations)
                .getAndDelete(redisKey);

        verify(objectMapper)
                .readValue(
                        serializedGrant,
                        InternalRequestGrant.class
                );
    }

    @Test
    void should_throw_not_found_when_grant_does_not_exist() {

        UUID internalRequestId =
                UUID.randomUUID();

        String redisKey =
                KEY_PREFIX + internalRequestId;

        when(props.keyPrefix())
                .thenReturn(KEY_PREFIX);

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.getAndDelete(redisKey))
                .thenReturn(null);

        assertThatThrownBy(
                () -> consumer.consume(internalRequestId)
        )
                .isInstanceOf(
                        InternalRequestGrantNotFoundException.class
                )
                .hasMessage(
                        "Internal request grant was not found, expired or already consumed"
                );

        verify(valueOperations)
                .getAndDelete(redisKey);

        verifyNoInteractions(objectMapper);
    }

    @Test
    void should_throw_not_found_when_grant_is_blank() {

        UUID internalRequestId =
                UUID.randomUUID();

        String redisKey =
                KEY_PREFIX + internalRequestId;

        when(props.keyPrefix())
                .thenReturn(KEY_PREFIX);

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.getAndDelete(redisKey))
                .thenReturn("   ");

        assertThatThrownBy(
                () -> consumer.consume(internalRequestId)
        )
                .isInstanceOf(
                        InternalRequestGrantNotFoundException.class
                );

        verify(valueOperations)
                .getAndDelete(redisKey);

        verifyNoInteractions(objectMapper);
    }

    @Test
    void should_throw_invalid_exception_when_grant_json_cannot_be_deserialized()
            throws Exception {

        UUID internalRequestId =
                UUID.randomUUID();

        String redisKey =
                KEY_PREFIX + internalRequestId;

        String invalidJson =
                "{invalid-json}";

        when(props.keyPrefix())
                .thenReturn(KEY_PREFIX);

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.getAndDelete(redisKey))
                .thenReturn(invalidJson);

        JsonProcessingException parsingException =
                mock(JsonProcessingException.class);

        when(objectMapper.readValue(
                invalidJson,
                InternalRequestGrant.class
        )).thenThrow(parsingException);

        assertThatThrownBy(
                () -> consumer.consume(internalRequestId)
        )
                .isInstanceOf(
                        InternalRequestGrantInvalidException.class
                )
                .hasMessage(
                        "Internal request grant is invalid"
                )
                .hasCause(parsingException);

        verify(valueOperations)
                .getAndDelete(redisKey);
    }

    @Test
    void should_throw_authentication_service_exception_when_redis_is_unavailable() {

        UUID internalRequestId =
                UUID.randomUUID();

        String redisKey =
                KEY_PREFIX + internalRequestId;

        DataAccessResourceFailureException redisException =
                new DataAccessResourceFailureException(
                        "Redis unavailable"
                );

        when(props.keyPrefix())
                .thenReturn(KEY_PREFIX);

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.getAndDelete(redisKey))
                .thenThrow(redisException);

        assertThatThrownBy(
                () -> consumer.consume(internalRequestId)
        )
                .isInstanceOf(
                        InternalRequestAuthenticationServiceException.class
                )
                .hasMessage(
                        "Internal request authentication service is unavailable"
                )
                .hasCause(redisException);

        verify(valueOperations)
                .getAndDelete(redisKey);

        verifyNoInteractions(objectMapper);
    }

    @Test
    void should_throw_invalid_exception_when_internal_request_id_is_null() {

        assertThatThrownBy(
                () -> consumer.consume(null)
        )
                .isInstanceOf(
                        InternalRequestGrantInvalidException.class
                )
                .hasMessage(
                        "Internal request id must not be null"
                );

        verifyNoInteractions(
                redisTemplate,
                objectMapper,
                props
        );
    }

    private InternalRequestGrant validGrant() {
        return InternalRequestGrant.builder()
                .issuer(InternalService.AUTH_SERVICE)
                .target("user-service")
                .method("POST")
                .path("/v1/users/internal/lookup")
                .createdAt(Instant.now())
                .build();
    }
}