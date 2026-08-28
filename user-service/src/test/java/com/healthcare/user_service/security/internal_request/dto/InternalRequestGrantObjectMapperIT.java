package com.healthcare.user_service.security.internal_request.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.user_service.security.internal_request.constant.InternalService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Internal request grant ObjectMapper integration tests")
class InternalRequestGrantObjectMapperIT {

    private static final Instant CREATED_AT =
            Instant.parse(
                    "2026-08-28T09:00:00Z"
            );

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void should_serialize_internal_request_grant_using_spring_object_mapper()
            throws Exception {

        InternalRequestGrant grant =
                InternalRequestGrant.builder()
                        .issuer(
                                InternalService.AUTH_SERVICE
                        )
                        .target(
                                "user-service"
                        )
                        .method(
                                "POST"
                        )
                        .path(
                                "/v1/users/internal/lookup"
                        )
                        .createdAt(
                                CREATED_AT
                        )
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
    void should_deserialize_internal_request_grant_using_spring_object_mapper()
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
                        CREATED_AT
                );
    }

    @Test
    void should_preserve_internal_request_grant_after_round_trip()
            throws Exception {

        InternalRequestGrant original =
                InternalRequestGrant.builder()
                        .issuer(
                                InternalService.AUTH_SERVICE
                        )
                        .target(
                                "user-service"
                        )
                        .method(
                                "POST"
                        )
                        .path(
                                "/v1/users/internal/lookup"
                        )
                        .createdAt(
                                CREATED_AT
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
                .isEqualTo(
                        original
                );
    }

    @Test
    void should_serialize_ai_service_using_stable_service_name()
            throws Exception {

        InternalRequestGrant grant =
                InternalRequestGrant.builder()
                        .issuer(
                                InternalService.AI_SERVICE
                        )
                        .target(
                                "user-service"
                        )
                        .method(
                                "POST"
                        )
                        .path(
                                "/v1/users/internal/auth-info"
                        )
                        .createdAt(
                                CREATED_AT
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
    void should_deserialize_ai_service_using_stable_service_name()
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

        assertThat(grant.method())
                .isEqualTo(
                        "POST"
                );

        assertThat(grant.path())
                .isEqualTo(
                        "/v1/users/internal/auth-info"
                );

        assertThat(grant.createdAt())
                .isEqualTo(
                        CREATED_AT
                );
    }
}