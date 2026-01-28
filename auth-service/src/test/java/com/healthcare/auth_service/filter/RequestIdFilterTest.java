package com.healthcare.auth_service.filter;

import com.healthcare.auth_service.config.properties.HeaderRequestIdProperties;
import com.healthcare.auth_service.exception_handler.exception.RequestIdAuthenticationException;
import com.healthcare.auth_service.service.interfacies.RequestIdService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(value = DisplayNameGenerator.ReplaceUnderscores.class)
class RequestIdFilterTest {

    @Mock
    private FilterChain filterChain;

    @Mock
    public HeaderRequestIdProperties props;

    @Mock
    private  RequestIdService requestIdService;

    @InjectMocks
    private RequestIdFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private final String HEADER_NAME = "test-header";

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void negative_should_continue_filter_if_request_id_is_null_or_blank(String value)  {

        when(props.name()).thenReturn(value);

        assertThrows(RequestIdAuthenticationException.class,
                () -> filter.doFilterInternal(request, response, filterChain));
    }

    @ParameterizedTest
    @ValueSource(strings = {"test_id", "3f8c2b7e-6a1d-4c9e-9b3a-7d2f1a6c4e90"})
    void negative_should_continue_filter_if_request_id_is_not_UUID_or_not_valid(String id)  {

        request.addHeader(HEADER_NAME, id);

        when(props.name()).thenReturn(HEADER_NAME);
        when(requestIdService.isRequestIdValid(id)).thenReturn(false);

        assertThrows(RequestIdAuthenticationException.class,
                () -> filter.doFilterInternal(request, response, filterChain));
    }

    @Test
    void positive_when_request_is_with_valid_request_ID_in_the_header() throws ServletException, IOException {

        String id = "3f8c2b7e-6a1d-4c9e-9b3a-7d2f1a6c4e90";

        request.addHeader(HEADER_NAME, id);

        when(props.name()).thenReturn(HEADER_NAME);
        when(requestIdService.isRequestIdValid(id)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}