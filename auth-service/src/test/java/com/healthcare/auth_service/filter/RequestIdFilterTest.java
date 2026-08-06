package com.healthcare.auth_service.filter;

import com.healthcare.auth_service.config.properties.HeaderRequestIdProperties;
import com.healthcare.auth_service.exception_handler.exception.RequestIdAuthenticationException;
import com.healthcare.auth_service.service.interfacies.RequestIdService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.UUID;

import static com.healthcare.auth_service.filter.context.constant.RequestContextAttributes.ATTR_REQUEST_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("RequestIdFilter tests: ")
class RequestIdFilterTest {

    private static final String HEADER_NAME =
            "test-header";

    private static final String VALID_REQUEST_ID =
            "3f8c2b7e-6a1d-4c9e-9b3a-7d2f1a6c4e90";

    @Mock
    private FilterChain filterChain;

    @Mock
    private HeaderRequestIdProperties props;

    @Mock
    private RequestIdService requestIdService;

    @InjectMocks
    private RequestIdFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        when(props.name())
                .thenReturn(HEADER_NAME);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            " ",
            "   ",
            "\t",
            "\n"
    })
    void should_throw_when_request_id_header_is_missing_or_blank(
            String value
    ) {
        if (value != null) {
            request.addHeader(
                    HEADER_NAME,
                    value
            );
        }

        assertThrows(
                RequestIdAuthenticationException.class,
                () -> filter.doFilterInternal(
                        request,
                        response,
                        filterChain
                )
        );

        assertThat(
                request.getAttribute(
                        ATTR_REQUEST_ID
                )
        ).isNull();

        verifyNoInteractions(requestIdService);
        verifyNoInteractions(filterChain);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "test_id",
            VALID_REQUEST_ID
    })
    void should_throw_when_request_id_is_invalid(
            String requestId
    ) {
        request.addHeader(
                HEADER_NAME,
                requestId
        );

        when(requestIdService.isRequestIdValid(requestId))
                .thenReturn(false);

        assertThrows(
                RequestIdAuthenticationException.class,
                () -> filter.doFilterInternal(
                        request,
                        response,
                        filterChain
                )
        );

        assertThat(
                request.getAttribute(
                        ATTR_REQUEST_ID
                )
        ).isNull();

        verify(requestIdService)
                .isRequestIdValid(requestId);

        verifyNoInteractions(filterChain);
    }

    @Test
    void should_store_request_id_as_uuid_and_continue_filter_chain_when_valid()
            throws ServletException, IOException {

        request.addHeader(
                HEADER_NAME,
                VALID_REQUEST_ID
        );

        when(requestIdService.isRequestIdValid(
                VALID_REQUEST_ID
        )).thenReturn(true);

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        Object requestIdAttribute =
                request.getAttribute(
                        ATTR_REQUEST_ID
                );

        assertThat(requestIdAttribute)
                .isInstanceOf(UUID.class)
                .isEqualTo(
                        UUID.fromString(
                                VALID_REQUEST_ID
                        )
                );

        verify(requestIdService)
                .isRequestIdValid(
                        VALID_REQUEST_ID
                );

        verify(filterChain)
                .doFilter(
                        request,
                        response
                );
    }

    @Test
    void should_normalize_request_id_before_validation_and_storing()
            throws ServletException, IOException {

        String requestIdWithSpaces =
                "  " + VALID_REQUEST_ID + "  ";

        request.addHeader(
                HEADER_NAME,
                requestIdWithSpaces
        );

        when(requestIdService.isRequestIdValid(
                VALID_REQUEST_ID
        )).thenReturn(true);

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        assertThat(
                request.getAttribute(
                        ATTR_REQUEST_ID
                )
        )
                .isInstanceOf(UUID.class)
                .isEqualTo(
                        UUID.fromString(
                                VALID_REQUEST_ID
                        )
                );

        verify(requestIdService)
                .isRequestIdValid(
                        VALID_REQUEST_ID
                );

        verify(filterChain)
                .doFilter(
                        request,
                        response
                );
    }

    @Test
    void should_not_store_request_id_when_validation_fails() {
        String invalidRequestId =
                "invalid-request-id";

        request.addHeader(
                HEADER_NAME,
                invalidRequestId
        );

        when(requestIdService.isRequestIdValid(
                invalidRequestId
        )).thenReturn(false);

        assertThrows(
                RequestIdAuthenticationException.class,
                () -> filter.doFilterInternal(
                        request,
                        response,
                        filterChain
                )
        );

        assertThat(
                request.getAttribute(
                        ATTR_REQUEST_ID
                )
        ).isNull();

        verify(requestIdService)
                .isRequestIdValid(
                        invalidRequestId
                );

        verifyNoInteractions(filterChain);
    }
}