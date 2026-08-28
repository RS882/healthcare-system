package com.healthcare.user_service.security.internal_request.validator;

import com.healthcare.user_service.exception_handler.exception.InternalRequestGrantInvalidException;
import com.healthcare.user_service.security.internal_request.constant.InternalService;
import com.healthcare.user_service.security.internal_request.dto.InternalRequestGrant;
import com.healthcare.user_service.security.internal_request.properties.InternalRequestConsumerProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Default internal request grant validator tests")
class DefaultInternalRequestGrantValidatorTest {

    private static final String SERVICE_NAME =
            "user-service";

    private static final String INTERNAL_LOOKUP_URL =
            "/v1/users/internal/lookup";

    private static final String METHOD =
            "POST";

    @Mock
    private InternalRequestConsumerProperties props;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private DefaultInternalRequestGrantValidator validator;

    @BeforeEach
    void setUp() {
        lenient()
                .when(props.serviceName())
                .thenReturn(SERVICE_NAME);

        lenient()
                .when(props.allowedIssuers())
                .thenReturn(Set.of(
                        InternalService.AUTH_SERVICE.serviceName()
                ));

        lenient()
                .when(request.getMethod())
                .thenReturn(METHOD);

        lenient()
                .when(request.getRequestURI())
                .thenReturn(INTERNAL_LOOKUP_URL);
    }

    @Test
    void should_validate_valid_internal_request_grant() {

        InternalRequestGrant grant = grant(
                InternalService.AUTH_SERVICE,
                SERVICE_NAME,
                METHOD,
                INTERNAL_LOOKUP_URL);

        assertThatCode(
                () -> validator.validate(
                        grant,
                        request
                )
        ).doesNotThrowAnyException();
    }

    @Test
    void should_throw_invalid_exception_when_grant_is_null() {

        assertThatThrownBy(
                () -> validator.validate(
                        null,
                        request
                )
        )
                .isInstanceOf(
                        InternalRequestGrantInvalidException.class
                )
                .hasMessage(
                        "Internal request grant is invalid"
                );
    }

    @Test
    void should_throw_invalid_exception_when_target_is_blank() {

        InternalRequestGrant grant = grant(
                InternalService.AUTH_SERVICE,
                "   ",
                METHOD,
                INTERNAL_LOOKUP_URL);

        assertInvalidGrant(
                grant,
                "Internal request grant is invalid"
        );
    }

    @Test
    void should_throw_invalid_exception_when_method_is_blank() {

        InternalRequestGrant grant = grant(
                InternalService.AUTH_SERVICE,
                SERVICE_NAME,
                "   ",
                INTERNAL_LOOKUP_URL);

        assertInvalidGrant(
                grant,
                "Internal request grant is invalid"
        );
    }

    @Test
    void should_throw_invalid_exception_when_path_is_blank() {

        InternalRequestGrant grant = grant(
                InternalService.AUTH_SERVICE,
                SERVICE_NAME,
                METHOD,
                "   ");

        assertInvalidGrant(
                grant,
                "Internal request grant is invalid"
        );
    }

    @Test
    void should_throw_invalid_exception_when_target_does_not_match_current_service() {

        InternalRequestGrant grant = grant(
                InternalService.AUTH_SERVICE,
                "another-service",
                METHOD,
                INTERNAL_LOOKUP_URL);

        assertInvalidGrant(
                grant,
                "Internal request target mismatch"
        );
    }

    @Test
    void should_throw_invalid_exception_when_http_method_does_not_match_request() {

        InternalRequestGrant grant = grant(
                InternalService.AUTH_SERVICE,
                SERVICE_NAME,
                "GET",
                INTERNAL_LOOKUP_URL);

        assertInvalidGrant(
                grant,
                "Internal request HTTP method mismatch"
        );
    }

    @Test
    void should_accept_http_method_case_insensitively() {

        when(request.getMethod())
                .thenReturn("post");

        InternalRequestGrant grant = grant(
                InternalService.AUTH_SERVICE,
                SERVICE_NAME,
                METHOD,
                INTERNAL_LOOKUP_URL);

        assertThatCode(
                () -> validator.validate(
                        grant,
                        request
                )
        ).doesNotThrowAnyException();
    }

    @Test
    void should_throw_invalid_exception_when_path_does_not_match_request_uri() {

        InternalRequestGrant grant = grant(
                InternalService.AUTH_SERVICE,
                SERVICE_NAME,
                METHOD,
                "/v1/users/internal/another");

        assertInvalidGrant(
                grant,
                "Internal request path mismatch"
        );
    }

    @Test
    void should_throw_invalid_exception_when_issuer_is_not_allowed() {

        InternalRequestGrant grant = grant(
                InternalService.AI_SERVICE,
                SERVICE_NAME,
                METHOD,
                INTERNAL_LOOKUP_URL);

        assertInvalidGrant(
                grant,
                "Internal request issuer is not allowed: "
                        + InternalService.AI_SERVICE.serviceName()
        );
    }

    @Test
    void should_validate_ai_service_when_it_is_in_allowlist() {

        when(props.allowedIssuers())
                .thenReturn(Set.of(
                        InternalService.AUTH_SERVICE.serviceName(),
                        InternalService.AI_SERVICE.serviceName()
                ));

        InternalRequestGrant grant = grant(
                InternalService.AI_SERVICE,
                SERVICE_NAME,
                METHOD,
                INTERNAL_LOOKUP_URL);

        assertThatCode(
                () -> validator.validate(
                        grant,
                        request
                )
        ).doesNotThrowAnyException();
    }

    @Test
    void should_throw_invalid_exception_when_issuer_is_null() {

        InternalRequestGrant grant = grant(
                null,
                SERVICE_NAME,
                METHOD,
                INTERNAL_LOOKUP_URL);

        assertInvalidGrant(
                grant,
                "Internal request grant is invalid"
        );
    }

    private void assertInvalidGrant(
            InternalRequestGrant grant,
            String expectedMessage
    ) {

        assertThatThrownBy(
                () -> validator.validate(
                        grant,
                        request
                )
        )
                .isInstanceOf(
                        InternalRequestGrantInvalidException.class
                )
                .hasMessage(expectedMessage);
    }

    private InternalRequestGrant grant(
            InternalService issuer,
            String target,
            String method,
            String path
    ) {
        return InternalRequestGrant.builder()
                .issuer(issuer)
                .target(target)
                .method(method)
                .path(path)
                .createdAt(Instant.now())
                .build();
    }
}