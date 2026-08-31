package com.healthcare.aiservice.security.internal_request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.aiservice.config.AbstractMongoRedisIntegrationTest;
import com.healthcare.aiservice.security.internal_request.dto.InternalRequestGrant;
import com.healthcare.aiservice.security.internal_request.interfaces.InternalRequestGrantIssuer;
import com.healthcare.aiservice.security.internal_request.properties.InternalRequestIssuerProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Redis internal request grant issuer integration tests: ")
class RedisInternalRequestGrantIssuerIntegrationTest
        extends AbstractMongoRedisIntegrationTest {

    private static final String TARGET_SERVICE = "user-service";

    private static final String PATH =
            "/api/v1/users/internal/42/auth-info";

    @Autowired
    private InternalRequestGrantIssuer grantIssuer;

    @Autowired
    private InternalRequestIssuerProperties properties;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void issue_ShouldStoreGrantInRedis_WithCorrectDataAndTtl()
            throws Exception {

        Instant beforeIssue = Instant.now();

        UUID internalRequestId = grantIssuer.issue(
                TARGET_SERVICE,
                HttpMethod.GET,
                PATH
        );

        Instant afterIssue = Instant.now();

        String redisKey = properties.keyPrefix() + internalRequestId;

        String storedJson = redisTemplate.opsForValue().get(redisKey);

        assertThat(storedJson).isNotNull();

        InternalRequestGrant storedGrant =
                objectMapper.readValue(
                        storedJson,
                        InternalRequestGrant.class
                );

        assertThat(storedGrant.issuer())
                .isEqualTo(properties.issuer());

        assertThat(storedGrant.target())
                .isEqualTo(TARGET_SERVICE);

        assertThat(storedGrant.method())
                .isEqualTo(HttpMethod.GET.name());

        assertThat(storedGrant.path())
                .isEqualTo(PATH);

        assertThat(storedGrant.createdAt())
                .isBetween(beforeIssue, afterIssue);

        Duration ttl =  Duration.ofSeconds(redisTemplate.getExpire(redisKey));

        assertThat(ttl).isNotNull();
        assertThat(ttl).isPositive();
        assertThat(ttl).isLessThanOrEqualTo(properties.ttl());
    }
}