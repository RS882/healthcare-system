package com.healthcare.user_service.security.internal_request.constant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.user_service.exception_handler.exception.UnknownInternalServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Internal service tests")
class InternalServiceTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    // =========================================================
    // serviceName()
    // =========================================================

    @Test
    void should_return_auth_service_name() {

        String serviceName =
                InternalService.AUTH_SERVICE.serviceName();

        assertThat(serviceName)
                .isEqualTo("auth-service");
    }

    @Test
    void should_return_ai_service_name() {

        String serviceName =
                InternalService.AI_SERVICE.serviceName();

        assertThat(serviceName)
                .isEqualTo("ai-service");
    }

    // =========================================================
    // fromServiceName()
    // =========================================================

    @Test
    void should_resolve_auth_service_from_service_name() {

        InternalService service =
                InternalService.fromServiceName(
                        "auth-service"
                );

        assertThat(service)
                .isEqualTo(
                        InternalService.AUTH_SERVICE
                );
    }

    @Test
    void should_resolve_ai_service_from_service_name() {

        InternalService service =
                InternalService.fromServiceName(
                        "ai-service"
                );

        assertThat(service)
                .isEqualTo(
                        InternalService.AI_SERVICE
                );
    }

    // =========================================================
    // Invalid service
    // =========================================================

    @Test
    void should_throw_unknown_internal_service_exception_for_unknown_service() {

        assertThatThrownBy(() ->
                InternalService.fromServiceName(
                        "unknown-service"
                )
        )
                .isInstanceOf(
                        UnknownInternalServiceException.class
                )
                .hasMessage(
                        "Unknown internal service: unknown-service"
                );
    }

    @Test
    void should_reject_java_enum_constant_as_service_name() {

        assertThatThrownBy(() ->
                InternalService.fromServiceName(
                        "AUTH_SERVICE"
                )
        )
                .isInstanceOf(
                        UnknownInternalServiceException.class
                )
                .hasMessage(
                        "Unknown internal service: AUTH_SERVICE"
                );
    }

    @Test
    void should_throw_unauthorized_status_for_unknown_service() {

        try {
            InternalService.fromServiceName(
                    "unknown-service"
            );

        } catch (UnknownInternalServiceException exception) {

            assertThat(exception.getStatus())
                    .isEqualTo(
                            HttpStatus.UNAUTHORIZED
                    );

            return;
        }

        throw new AssertionError(
                "Expected UnknownInternalServiceException"
        );
    }

    // =========================================================
    // Jackson serialization
    // =========================================================

    @Test
    void should_serialize_auth_service_using_service_name()
            throws Exception {

        String json =
                objectMapper.writeValueAsString(
                        InternalService.AUTH_SERVICE
                );

        assertThat(json)
                .isEqualTo(
                        "\"auth-service\""
                );
    }

    @Test
    void should_serialize_ai_service_using_service_name()
            throws Exception {

        String json =
                objectMapper.writeValueAsString(
                        InternalService.AI_SERVICE
                );

        assertThat(json)
                .isEqualTo(
                        "\"ai-service\""
                );
    }

    // =========================================================
    // Jackson deserialization
    // =========================================================

    @Test
    void should_deserialize_auth_service_from_service_name()
            throws Exception {

        InternalService service =
                objectMapper.readValue(
                        "\"auth-service\"",
                        InternalService.class
                );

        assertThat(service)
                .isEqualTo(
                        InternalService.AUTH_SERVICE
                );
    }

    @Test
    void should_deserialize_ai_service_from_service_name()
            throws Exception {

        InternalService service =
                objectMapper.readValue(
                        "\"ai-service\"",
                        InternalService.class
                );

        assertThat(service)
                .isEqualTo(
                        InternalService.AI_SERVICE
                );
    }

    // =========================================================
    // Jackson invalid deserialization
    // =========================================================

    @Test
    void should_fail_deserialization_for_unknown_service() {

        assertThatThrownBy(() ->
                objectMapper.readValue(
                        "\"unknown-service\"",
                        InternalService.class
                )
        )
                .hasRootCauseInstanceOf(
                        UnknownInternalServiceException.class
                )
                .rootCause()
                .hasMessage(
                        "Unknown internal service: unknown-service"
                );
    }

    @Test
    void should_fail_deserialization_for_java_enum_constant() {

        assertThatThrownBy(() ->
                objectMapper.readValue(
                        "\"AUTH_SERVICE\"",
                        InternalService.class
                )
        )
                .hasRootCauseInstanceOf(
                        UnknownInternalServiceException.class
                )
                .rootCause()
                .hasMessage(
                        "Unknown internal service: AUTH_SERVICE"
                );
    }
}