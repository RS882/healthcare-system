package com.healthcare.billing.exception;

import com.healthcare.billing.exception.dto.ErrorResponse;
import com.healthcare.billing.exception.error_code.ErrorCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/invoices/1");
    }

    @Test
    void shouldHandleRestExceptionAndAddRequestPath() {
        InvoiceNotFoundException exception = new InvoiceNotFoundException(1L);

        ResponseEntity<ErrorResponse> response =
                handler.handleException(exception, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertAll(
                () -> assertEquals(404, response.getBody().status()),
                () -> assertEquals(ErrorCode.INVOICE_NOT_FOUND.name(), response.getBody().error()),
                () -> assertEquals("Invoice with id 1 not found", response.getBody().message()),
                () -> assertEquals("/api/v1/invoices/1", response.getBody().path()),
                () -> assertNotNull(response.getBody().validationErrors()),
                () -> assertTrue(response.getBody().validationErrors().isEmpty())
        );
    }

    @Test
    void shouldHandleInvalidRequestBody() {
        HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);

        ResponseEntity<ErrorResponse> response =
                handler.handleHttpMessageNotReadableException(exception, request);

        assertError(
                response,
                HttpStatus.BAD_REQUEST,
                ErrorCode.INVALID_REQUEST_BODY
        );
    }

    @Test
    void shouldHandleInvalidRequestParameter() {
        MethodArgumentTypeMismatchException exception =
                mock(MethodArgumentTypeMismatchException.class);
        when(exception.getName()).thenReturn("id");
        when(exception.getValue()).thenReturn("abc");

        ResponseEntity<ErrorResponse> response =
                handler.handleMethodArgumentTypeMismatchException(exception, request);

        assertError(
                response,
                HttpStatus.BAD_REQUEST,
                ErrorCode.INVALID_REQUEST_PARAMETER
        );
    }

    @Test
    void shouldHandleMethodArgumentValidationErrors() {
        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("request", "patientId", "must be greater than 0"),
                new FieldError("request", "items", "must not be empty")
        ));

        ResponseEntity<ErrorResponse> response =
                handler.handleMethodArgumentNotValidException(exception, request);

        assertError(response, HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR);
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().validationErrors().size());
        assertTrue(response.getBody().validationErrors().stream()
                .anyMatch(error -> error.field().equals("patientId")));
        assertTrue(response.getBody().validationErrors().stream()
                .anyMatch(error -> error.field().equals("items")));
    }

    @Test
    void shouldHandleConstraintViolation() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path propertyPath = mock(Path.class);
        when(propertyPath.toString()).thenReturn("getById.id");
        when(violation.getPropertyPath()).thenReturn(propertyPath);
        when(violation.getMessage()).thenReturn("must be greater than 0");

        ConstraintViolationException exception =
                new ConstraintViolationException(Set.of(violation));

        ResponseEntity<ErrorResponse> response =
                handler.handleConstraintViolationException(exception, request);

        assertError(response, HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR);
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().validationErrors().size());
        assertTrue(response.getBody().validationErrors().stream()
                .anyMatch(error ->
                        error.field().equals("getById.id")
                                && error.message().equals("must be greater than 0")
                ));
    }

    @Test
    void shouldHideUnexpectedExceptionDetailsFromClient() {
        RuntimeException exception = new RuntimeException("database password leaked");

        ResponseEntity<ErrorResponse> response =
                handler.handleGenericException(exception, request);

        assertError(
                response,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR
        );
        assertNotNull(response.getBody());
        assertNotEquals("database password leaked", response.getBody().message());
    }

    private void assertError(
            ResponseEntity<ErrorResponse> response,
            HttpStatus expectedStatus,
            ErrorCode expectedErrorCode
    ) {
        assertEquals(expectedStatus, response.getStatusCode());
        assertNotNull(response.getBody());
        assertAll(
                () -> assertEquals(expectedStatus.value(), response.getBody().status()),
                () -> assertEquals(expectedErrorCode.name(), response.getBody().error()),
                () -> assertEquals(expectedErrorCode.getDefaultMessage(), response.getBody().message()),
                () -> assertEquals("/api/v1/invoices/1", response.getBody().path()),
                () -> assertNotNull(response.getBody().validationErrors())
        );
    }
}
