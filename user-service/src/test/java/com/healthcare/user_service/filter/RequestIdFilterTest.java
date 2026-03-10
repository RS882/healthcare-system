package com.healthcare.user_service.filter;

import com.healthcare.user_service.config.properties.HeaderRequestIdProperties;
import com.healthcare.user_service.exception_handler.exception.RequestIdException;
import com.healthcare.user_service.service.interfacies.RequestIdService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

import static com.healthcare.user_service.filter.security.constant.AttrNames.ATTR_REQUEST_ID;
import static com.healthcare.user_service.support.TestDataFactory.requestId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class RequestIdFilterTest {

    @Mock
    private FilterChain filterChain;

    @Mock
    private HeaderRequestIdProperties props;

    @Mock
    private RequestIdService requestIdService;

    @Mock
    private HandlerExceptionResolver handlerExceptionResolver;

    @InjectMocks
    private RequestIdFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    private static final String HEADER_NAME = "test-header";

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void negative_should_resolve_exception_if_request_id_is_null_or_blank(String value)
            throws ServletException, IOException {

        when(props.name()).thenReturn(HEADER_NAME);

        if (value != null) {
            request.addHeader(HEADER_NAME, value);
        }

        filter.doFilterInternal(request, response, filterChain);

        verify(handlerExceptionResolver).resolveException(
                eq(request),
                eq(response),
                isNull(),
                any(RequestIdException.class)
        );
        verify(filterChain, never()).doFilter(request, response);
    }

    @ParameterizedTest
    @ValueSource(strings = {"test_id", "3f8c2b7e-6a1d-4c9e-9b3a-7d2f1a6c4e90"})
    void negative_should_resolve_exception_if_request_id_is_not_uuid_or_not_valid(String id)
            throws ServletException, IOException {

        request.addHeader(HEADER_NAME, id);

        when(props.name()).thenReturn(HEADER_NAME);
        when(requestIdService.isRequestIdValid(id)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(requestIdService).isRequestIdValid(id);
        verify(handlerExceptionResolver).resolveException(
                eq(request),
                eq(response),
                isNull(),
                any(RequestIdException.class)
        );
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void positive_when_request_is_with_valid_request_id_in_the_header()
            throws ServletException, IOException {

        String id = requestId().toString();

        request.addHeader(HEADER_NAME, id);

        when(props.name()).thenReturn(HEADER_NAME);
        when(requestIdService.isRequestIdValid(id)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        Object ridAttr = request.getAttribute(ATTR_REQUEST_ID);

        assertInstanceOf(String.class, ridAttr);
        assertEquals(id, ridAttr.toString());

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(handlerExceptionResolver);
    }
}