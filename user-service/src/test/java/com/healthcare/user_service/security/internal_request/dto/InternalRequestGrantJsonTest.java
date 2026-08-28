package com.healthcare.user_service.security.internal_request.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.healthcare.user_service.security.internal_request.constant.InternalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Internal request grant JSON tests")
class InternalRequestGrantJsonTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        objectMapper.registerModule(
                new JavaTimeModule()
        );

        objectMapper.disable(
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
        );
    }

    @Test
    void should_serialize_internal_request_grant_using_stable_wire_format()
            throws Exception {

        Instant createdAt =
                Instant.parse(
                        "2026-08-28T09:00:00Z"
                );

        InternalRequestGrant grant =
                InternalRequestGrant.builder()
                        .issuer(
                                InternalService.AUTH_SERVICE
                        )
                        .target("user-service")
                        .method("POST")
                        .path(
                                "/v1/users/internal/lookup"
                        )
                        .createdAt(createdAt)
                        .build();

        String json =
                objectMapper.writeValueAsString(
                        grant
                );

        assertThat(json)
                .contains(
                        "\"issuer\":\"auth-service\""
                )
                .contains(
                        "\"target\":\"user-service\""
                )
                .contains(
                        "\"method\":\"POST\""
                )
                .contains(
                        "\"path\":\"/v1/users/internal/lookup\""
                )
                .contains(
                        "\"createdAt\":\"2026-08-28T09:00:00Z\""
                )
                .doesNotContain(
                        "\"issuer\":\"AUTH_SERVICE\""
                );
    }

    @Test
    void should_deserialize_internal_request_grant_using_stable_wire_format()
            throws Exception {

        String json = """
                {
                  "issuer": "auth-service",
                  "target": "user-service",
                  "method": "POST",
                  "path": "/v1/users/internal/lookup",
                  "createdAt": "2026-08-28T09:00:00Z"
                }
                """;

        InternalRequestGrant grant =
                objectMapper.readValue(
                        json,
                        InternalRequestGrant.class
                );

        assertThat(grant.issuer())
                .isEqualTo(
                        InternalService.AUTH_SERVICE
                );

        assertThat(grant.target())
                .isEqualTo(
                        "user-service"
                );

        assertThat(grant.method())
                .isEqualTo(
                        "POST"
                );

        assertThat(grant.path())
                .isEqualTo(
                        "/v1/users/internal/lookup"
                );

        assertThat(grant.createdAt())
                .isEqualTo(
                        Instant.parse(
                                "2026-08-28T09:00:00Z"
                        )
                );
    }

    @Test
    void should_preserve_internal_request_grant_after_serialization_round_trip()
            throws Exception {

        InternalRequestGrant original =
                InternalRequestGrant.builder()
                        .issuer(
                                InternalService.AUTH_SERVICE
                        )
                        .target("user-service")
                        .method("POST")
                        .path(
                                "/v1/users/internal/lookup"
                        )
                        .createdAt(
                                Instant.parse(
                                        "2026-08-28T09:00:00Z"
                                )
                        )
                        .build();

        String json =
                objectMapper.writeValueAsString(
                        original
                );

        InternalRequestGrant restored =
                objectMapper.readValue(
                        json,
                        InternalRequestGrant.class
                );

        assertThat(restored)
                .isEqualTo(original);
    }

    @Test
    void should_serialize_ai_service_as_service_name()
            throws Exception {

        InternalRequestGrant grant =
                InternalRequestGrant.builder()
                        .issuer(
                                InternalService.AI_SERVICE
                        )
                        .target("user-service")
                        .method("POST")
                        .path(
                                "/v1/users/internal/auth-info"
                        )
                        .createdAt(
                                Instant.parse(
                                        "2026-08-28T09:00:00Z"
                                )
                        )
                        .build();

        String json =
                objectMapper.writeValueAsString(
                        grant
                );

        assertThat(json)
                .contains(
                        "\"issuer\":\"ai-service\""
                )
                .doesNotContain(
                        "\"issuer\":\"AI_SERVICE\""
                );
    }

    @Test
    void should_deserialize_ai_service_from_stable_service_name()
            throws Exception {

        String json = """
                {
                  "issuer": "ai-service",
                  "target": "user-service",
                  "method": "POST",
                  "path": "/v1/users/internal/auth-info",
                  "createdAt": "2026-08-28T09:00:00Z"
                }
                """;

        InternalRequestGrant grant =
                objectMapper.readValue(
                        json,
                        InternalRequestGrant.class
                );

        assertThat(grant.issuer())
                .isEqualTo(
                        InternalService.AI_SERVICE
                );

        assertThat(grant.target())
                .isEqualTo(
                        "user-service"
                );
    }
}