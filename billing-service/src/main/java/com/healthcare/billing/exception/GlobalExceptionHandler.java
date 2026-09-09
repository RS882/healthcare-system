package com.healthcare.billing.exception;

import com.healthcare.billing.exception.dto.ErrorResponse;
import com.healthcare.billing.exception.dto.ValidationError;
import com.healthcare.billing.exception.error_code.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RestException.class)
    public ResponseEntity<ErrorResponse> handleException(RestException ex, HttpServletRequest request) {

        ErrorResponse response = ex.getResponse();

        logRestException(ex,request);

        return buildErrorResponse(
                ex.getStatus(),
                response.error(),
                response.message(),
                request,
                response.validationErrors()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        log.warn(
                "Invalid request body. path={}",
                request.getRequestURI()
        );

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.INVALID_REQUEST_BODY,
                request
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        Set<ValidationError> validationErrors =
                ex.getConstraintViolations()
                        .stream()
                        .map(violation -> new ValidationError(
                                violation.getPropertyPath().toString(),
                                violation.getMessage()
                        ))
                        .collect(Collectors.toSet());

        log.warn(
                "Constraint violation. path={}, errors={}",
                request.getRequestURI(),
                validationErrors
        );

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_ERROR,
                request,
                validationErrors
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        log.warn(
                "Invalid request parameter. path={}, parameter={}, value={}",
                request.getRequestURI(),
                ex.getName(),
                ex.getValue()
        );

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.INVALID_REQUEST_PARAMETER,
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Set<ValidationError> validationErrors =
                ex.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(error -> new ValidationError(
                                error.getField(),
                                error.getDefaultMessage()
                        ))
                        .collect(Collectors.toSet());

        log.warn(
                "Request validation failed. path={}, errors={}",
                request.getRequestURI(),
                validationErrors
        );

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_ERROR,
                request,
                validationErrors
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error(
                "Unexpected error. path={}",
                request.getRequestURI(),
                ex
        );

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR,
                request
        );
    }

    private void logRestException(
            RestException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = ex.getStatus();

        if (status.is5xxServerError()) {
            log.error(
                    "REST exception. status={}, path={}, error={}, message={}",
                    status.value(),
                    request.getRequestURI(),
                    ex.getResponse().error(),
                    ex.getResponse().message(),
                    ex
            );
            return;
        }

        log.warn(
                "REST exception. status={}, path={}, error={}, message={}",
                status.value(),
                request.getRequestURI(),
                ex.getResponse().error(),
                ex.getResponse().message()
        );
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status,
            String error,
            String message,
            HttpServletRequest request
    ) {
        return buildErrorResponse(status, error, message, request, Set.of());
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status,
            ErrorCode errorCode,
            HttpServletRequest request
    ) {
        return buildErrorResponse(status, errorCode, request, Set.of());
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status,
            ErrorCode errorCode,
            HttpServletRequest request,
            Set<ValidationError> validationErrors
    ) {
        return buildErrorResponse(status, errorCode.name(), errorCode.getDefaultMessage(), request, validationErrors);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status,
            String error,
            String message,
            HttpServletRequest request,
            Set<ValidationError> validationErrors
    ) {
        return ResponseEntity
                .status(status)
                .body(
                        ErrorResponse.builder()
                                .timestamp(Instant.now())
                                .status(status.value())
                                .error(error)
                                .message(message)
                                .path(request.getRequestURI())
                                .validationErrors(
                                        validationErrors == null
                                                ? Set.of()
                                                : validationErrors
                                )
                                .build()
                );
    }
}
