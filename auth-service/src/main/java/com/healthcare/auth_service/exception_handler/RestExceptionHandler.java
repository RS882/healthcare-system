package com.healthcare.auth_service.exception_handler;

import com.healthcare.auth_service.exception_handler.dto.ErrorResponse;
import com.healthcare.auth_service.exception_handler.dto.ValidationError;
import com.healthcare.auth_service.exception_handler.exception.RestException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class RestExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Set<ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> ValidationError.builder()
                        .field(err.getField())
                        .message(err.getDefaultMessage())
                        .build())
                .collect(Collectors.toSet());

        HttpStatus status = HttpStatus.BAD_REQUEST;

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(List.of("The error of validation of the request"))
                .validationErrors(validationErrors)
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(List.of(ex.getMessage()))
                .path(request.getRequestURI())
                .validationErrors(null)
                .build();

        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(AuthenticationException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(List.of(ex.getMessage()))
                .path(request.getRequestURI())
                .validationErrors(null)
                .build();

        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(RestException.class)
    public ResponseEntity<ErrorResponse> handleException(RestException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ex.getResponse();

        errorResponse.setPath(request.getRequestURI());
        log.error("REST Error: {}", errorResponse, ex);

        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleException(RuntimeException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(List.of(ex.getMessage()))
                .path(request.getRequestURI())
                .build();
        ;

        log.error("Some error: {}", errorResponse, ex);

        return new ResponseEntity<>(errorResponse, status);
    }
}
