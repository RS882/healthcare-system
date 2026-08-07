package com.healthcare.auth_service.service.internal_request;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.auth_service.exception_handler.exception.InternalRequestGrantCreationException;
import com.healthcare.auth_service.exception_handler.exception.InternalRequestGrantUnavailableException;
import com.healthcare.auth_service.service.internal_request.dto.InternalRequestGrant;
import com.healthcare.auth_service.service.internal_request.interfaces.InternalRequestIdGenerator;
import com.healthcare.auth_service.service.internal_request.properties.InternalRequestIssuerProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Redis internal request grant issuer tests: ")
class RedisInternalRequestGrantIssuerTest {

    private static final String ISSUER =
            "auth-service";

    private static final String TARGET_SERVICE =
            "user-service";

    private static final String REDIS_KEY_PREFIX =
            "internal-request:";

    private static final String HEADER_NAME =
            "X-Internal-Request-Id";

    private static final Duration TTL =
            Duration.ofSeconds(30);

    private static final String LOOKUP_PATH =
            "/api/v1/users/lookup/internal";

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private InternalRequestIdGenerator requestIdGenerator;

    private InternalRequestIssuerProperties properties;

    private RedisInternalRequestGrantIssuer issuer;

    @BeforeEach
    void setUp() {
        properties =
                new InternalRequestIssuerProperties(
                        ISSUER,
                        REDIS_KEY_PREFIX,
                        HEADER_NAME,
                        TTL
                );

        issuer =
                new RedisInternalRequestGrantIssuer(
                        redisTemplate,
                        objectMapper,
                        properties,
                        requestIdGenerator
                );
    }

    @Test
    void issue_ShouldStoreSerializedGrantAndReturnRequestId()
            throws Exception {

        UUID requestId = UUID.randomUUID();

        String serializedGrant =
                """
                {
                  "issuer":"auth-service",
                  "target":"user-service",
                  "method":"POST",
                  "path":"/api/v1/users/lookup/internal"
                }
                """;

        when(objectMapper.writeValueAsString(
                any(InternalRequestGrant.class)
        )).thenReturn(serializedGrant);

        when(requestIdGenerator.generate())
                .thenReturn(requestId);

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.setIfAbsent(
                REDIS_KEY_PREFIX + requestId,
                serializedGrant,
                TTL
        )).thenReturn(true);

        UUID result =
                issuer.issue(
                        TARGET_SERVICE,
                        HttpMethod.POST,
                        LOOKUP_PATH
                );

        assertThat(result)
                .isEqualTo(requestId);

        verify(valueOperations)
                .setIfAbsent(
                        REDIS_KEY_PREFIX + requestId,
                        serializedGrant,
                        TTL
                );
    }

    @Test
    void issue_ShouldCreateGrantWithNormalizedTargetAndPath()
            throws Exception {

        UUID requestId = UUID.randomUUID();

        when(objectMapper.writeValueAsString(
                any(InternalRequestGrant.class)
        )).thenReturn("{}");

        when(requestIdGenerator.generate())
                .thenReturn(requestId);

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.setIfAbsent(
                anyString(),
                eq("{}"),
                eq(TTL)
        )).thenReturn(true);

        issuer.issue(
                "  user-service  ",
                HttpMethod.GET,
                "  api/v1/users/42/auth-info?includeRoles=true  "
        );

        ArgumentCaptor<InternalRequestGrant> grantCaptor =
                ArgumentCaptor.forClass(
                        InternalRequestGrant.class
                );

        verify(objectMapper)
                .writeValueAsString(
                        grantCaptor.capture()
                );

        InternalRequestGrant grant =
                grantCaptor.getValue();

        assertThat(grant.issuer())
                .isEqualTo(ISSUER);

        assertThat(grant.target())
                .isEqualTo(TARGET_SERVICE);

        assertThat(grant.method())
                .isEqualTo(HttpMethod.GET.name());

        assertThat(grant.path())
                .isEqualTo(
                        "/api/v1/users/42/auth-info"
                );

        assertThat(grant.createdAt())
                .isNotNull();
    }

    @Test
    void issue_ShouldGenerateNewRequestId_WhenRedisKeyAlreadyExists()
            throws Exception {

        UUID firstRequestId =
                UUID.randomUUID();

        UUID secondRequestId =
                UUID.randomUUID();

        when(objectMapper.writeValueAsString(
                any(InternalRequestGrant.class)
        )).thenReturn("{}");

        when(requestIdGenerator.generate())
                .thenReturn(
                        firstRequestId,
                        secondRequestId
                );

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.setIfAbsent(
                REDIS_KEY_PREFIX + firstRequestId,
                "{}",
                TTL
        )).thenReturn(false);

        when(valueOperations.setIfAbsent(
                REDIS_KEY_PREFIX + secondRequestId,
                "{}",
                TTL
        )).thenReturn(true);

        UUID result =
                issuer.issue(
                        TARGET_SERVICE,
                        HttpMethod.POST,
                        LOOKUP_PATH
                );

        assertThat(result)
                .isEqualTo(secondRequestId);

        verify(requestIdGenerator, times(2))
                .generate();

        verify(valueOperations)
                .setIfAbsent(
                        REDIS_KEY_PREFIX + firstRequestId,
                        "{}",
                        TTL
                );

        verify(valueOperations)
                .setIfAbsent(
                        REDIS_KEY_PREFIX + secondRequestId,
                        "{}",
                        TTL
                );
    }

    @Test
    void issue_ShouldThrowUnavailableException_WhenAllAttemptsFail()
            throws Exception {

        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        UUID third = UUID.randomUUID();
        UUID fourth = UUID.randomUUID();
        UUID fifth = UUID.randomUUID();

        when(objectMapper.writeValueAsString(
                any(InternalRequestGrant.class)
        )).thenReturn("{}");

        when(requestIdGenerator.generate())
                .thenReturn(
                        first,
                        second,
                        third,
                        fourth,
                        fifth
                );

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.setIfAbsent(
                anyString(),
                eq("{}"),
                eq(TTL)
        )).thenReturn(false);

        assertThatThrownBy(
                () -> issuer.issue(
                        TARGET_SERVICE,
                        HttpMethod.POST,
                        LOOKUP_PATH
                )
        )
                .isInstanceOf(
                        InternalRequestGrantUnavailableException.class
                )
                .hasMessage(
                        "Failed to generate and store a unique internal request grant"
                )
                .satisfies(exception -> {
                    InternalRequestGrantUnavailableException actual =
                            (InternalRequestGrantUnavailableException) exception;

                    assertThat(actual.getStatus())
                            .isEqualTo(
                                    HttpStatus.SERVICE_UNAVAILABLE
                            );
                });

        verify(requestIdGenerator, times(5))
                .generate();

        verify(valueOperations, times(5))
                .setIfAbsent(
                        anyString(),
                        eq("{}"),
                        eq(TTL)
                );
    }

    @Test
    void issue_ShouldThrowUnavailableException_WhenRedisFails()
            throws Exception {

        UUID requestId = UUID.randomUUID();

        when(objectMapper.writeValueAsString(
                any(InternalRequestGrant.class)
        )).thenReturn("{}");

        when(requestIdGenerator.generate())
                .thenReturn(requestId);

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        DataAccessResourceFailureException redisException =
                new DataAccessResourceFailureException(
                        "Redis unavailable"
                );

        when(valueOperations.setIfAbsent(
                REDIS_KEY_PREFIX + requestId,
                "{}",
                TTL
        )).thenThrow(redisException);

        assertThatThrownBy(
                () -> issuer.issue(
                        TARGET_SERVICE,
                        HttpMethod.POST,
                        LOOKUP_PATH
                )
        )
                .isInstanceOf(
                        InternalRequestGrantUnavailableException.class
                )
                .hasMessage(
                        "Failed to store internal request grant"
                )
                .hasCause(redisException)
                .satisfies(exception -> {
                    InternalRequestGrantUnavailableException actual =
                            (InternalRequestGrantUnavailableException) exception;

                    assertThat(actual.getStatus())
                            .isEqualTo(
                                    HttpStatus.SERVICE_UNAVAILABLE
                            );
                });
    }

    @Test
    void issue_ShouldThrowCreationException_WhenSerializationFails()
            throws Exception {

        JsonProcessingException serializationException =
                mock(JsonProcessingException.class);

        when(objectMapper.writeValueAsString(
                any(InternalRequestGrant.class)
        )).thenThrow(serializationException);

        assertThatThrownBy(
                () -> issuer.issue(
                        TARGET_SERVICE,
                        HttpMethod.POST,
                        LOOKUP_PATH
                )
        )
                .isInstanceOf(
                        InternalRequestGrantCreationException.class
                )
                .hasMessage(
                        "Failed to serialize internal request grant"
                )
                .hasCause(serializationException)
                .satisfies(exception -> {
                    InternalRequestGrantCreationException actual =
                            (InternalRequestGrantCreationException) exception;

                    assertThat(actual.getStatus())
                            .isEqualTo(
                                    HttpStatus.INTERNAL_SERVER_ERROR
                            );
                });

        verifyNoInteractions(
                redisTemplate,
                requestIdGenerator
        );
    }

    @Test
    void issue_ShouldRejectBlankTargetService() {

        assertThatThrownBy(
                () -> issuer.issue(
                        " ",
                        HttpMethod.POST,
                        LOOKUP_PATH
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Target service must not be blank"
                );

        verifyNoInteractions(
                objectMapper,
                redisTemplate,
                requestIdGenerator
        );
    }

    @Test
    void issue_ShouldRejectNullHttpMethod() {

        assertThatThrownBy(
                () -> issuer.issue(
                        TARGET_SERVICE,
                        null,
                        LOOKUP_PATH
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "HTTP method must not be null"
                );

        verifyNoInteractions(
                objectMapper,
                redisTemplate,
                requestIdGenerator
        );
    }

    @Test
    void issue_ShouldRejectBlankPath() {

        assertThatThrownBy(
                () -> issuer.issue(
                        TARGET_SERVICE,
                        HttpMethod.POST,
                        " "
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Request path must not be blank"
                );

        verifyNoInteractions(
                objectMapper,
                redisTemplate,
                requestIdGenerator
        );
    }
}