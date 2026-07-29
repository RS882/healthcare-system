package com.healthcare.aiservice.exception;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.healthcare.aiservice.exception.dto.ErrorResponse;
import com.healthcare.aiservice.exception.dto.ValidationError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        Set<ValidationError> validationErrors = ex.getConstraintViolations()
                .stream()
                .map(this::toValidationError)
                .collect(Collectors.toSet());

        log.warn(
                "Constraint validation failed. path={}, validationErrors={}",
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        Set<ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ValidationError(
                        error.getField(),
                        error.getDefaultMessage()
                ))
                .collect(Collectors.toSet());

        log.warn(
                "Validation failed. path={}, validationErrors={}",
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

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ErrorResponse> handleRequestParameterException(
            Exception ex,
            HttpServletRequest request
    ) {
        final HttpStatus status = HttpStatus.BAD_REQUEST;
        final ErrorCode errorCode = ErrorCode.INVALID_REQUEST_PARAMETER;

        String message;

        if (ex instanceof MethodArgumentTypeMismatchException mismatch) {
            message = buildTypeMismatchMessage(mismatch);

        } else if (ex instanceof MissingServletRequestParameterException missing) {
            message = buildMissingParameterMessage(missing);

        } else {
            message = errorCode.getDefaultMessage();
        }

        log.warn(
                "Invalid request parameter. path={}, message={}",
                request.getRequestURI(),
                message
        );

        return buildErrorResponse(
                status,
                errorCode.name(),
                message,
                request
        );
    }

    @ExceptionHandler(NonTransientAiException.class)
    public ResponseEntity<ErrorResponse> handleAiProviderException(
            NonTransientAiException ex,
            HttpServletRequest request
    ) {

        log.error(
                "AI provider failed. path={}",
                request.getRequestURI(),
                ex
        );

        return buildErrorResponse(
                HttpStatus.BAD_GATEWAY,
                ErrorCode.AI_PROVIDER_ERROR,
                request);
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ErrorResponse> handleAiConnectionException(
            ResourceAccessException ex,
            HttpServletRequest request
    ) {
        log.error(
                "AI provider unavailable. path={}, message={}",
                request.getRequestURI(),
                ex.getMessage(),
                ex
        );

        return buildErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                ErrorCode.AI_PROVIDER_UNAVAILABLE,
                request
        );
    }

    @ExceptionHandler(MismatchedInputException.class)
    public ResponseEntity<ErrorResponse> handleAiResponseParsingException(
            MismatchedInputException ex,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.BAD_GATEWAY,
                ErrorCode.AI_RESPONSE_UNEXPECTED,
                request
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        log.warn(
                "Invalid request body. path={}, message={}",
                request.getRequestURI(),
                buildRequestBodyErrorMessage(ex)
        );

        return buildErrorResponse(
                status,
                ErrorCode.INVALID_REQUEST_BODY.name(),
                buildRequestBodyErrorMessage(ex),
                request
        );
    }

    @ExceptionHandler(AiResponseParsingException.class)
    public ResponseEntity<ErrorResponse> handleAiResponseParsingException(
            AiResponseParsingException ex,
            HttpServletRequest request
    ) {

        HttpStatus status = HttpStatus.BAD_GATEWAY;

        log.error(
                "AI response parsing failed. path={}, rawResponse={}, extractedJson={}",
                request.getRequestURI(),
                ex.getRawResponse(),
                ex.getExtractedJson(),
                ex
        );

        return buildErrorResponse(
                status,
                ErrorCode.AI_RESPONSE_PARSING_ERROR,
                request
        );
    }

    @ExceptionHandler(RestException.class)
    public ResponseEntity<ErrorResponse> handleException(RestException ex, HttpServletRequest request) {

        ErrorResponse response = ex.getResponse();

        log.error(
                "REST exception. path={}, response={}",
                request.getRequestURI(),
                response,
                ex
        );

        return buildErrorResponse(
                ex.getStatus(),
                response.error(),
                response.message(),
                request,
                response.validationErrors()
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

    private ValidationError toValidationError(
            ConstraintViolation<?> violation
    ) {
        return new ValidationError(
                extractPropertyName(violation),
                violation.getMessage()
        );
    }

    private String extractPropertyName(
            ConstraintViolation<?> violation
    ) {
        String propertyPath = violation
                .getPropertyPath()
                .toString();

        int lastDotIndex = propertyPath.lastIndexOf('.');

        if (lastDotIndex < 0) {
            return propertyPath;
        }

        return propertyPath.substring(lastDotIndex + 1);
    }

    private String buildTypeMismatchMessage(
            MethodArgumentTypeMismatchException ex
    ) {
        return "Invalid value '%s' for parameter '%s'"
                .formatted(
                        ex.getValue(),
                        ex.getName()
                );
    }

    private String buildMissingParameterMessage(
            MissingServletRequestParameterException ex
    ) {
        if (ex.isMissingAfterConversion()) {
            return "Request parameter '%s' must not be blank"
                    .formatted(ex.getParameterName());
        }

        return "Required request parameter '%s' is missing"
                .formatted(ex.getParameterName());
    }

    private String buildRequestBodyErrorMessage(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();

        String message;

        if (cause instanceof InvalidFormatException ife) {

            Class<?> targetType = ife.getTargetType();
            String field = ife.getPath().isEmpty()
                    ? "unknown"
                    : ife.getPath().get(0).getFieldName();

            if (targetType.isEnum()) {

                String allowedValues = Arrays.stream(targetType.getEnumConstants())
                        .map(Object::toString)
                        .collect(Collectors.joining(", "));

                message = String.format(
                        "Invalid value '%s' for field '%s'. Allowed values: [%s].",
                        ife.getValue(),
                        field,
                        allowedValues
                );
            } else {

                message = String.format(
                        "Invalid value '%s' for field '%s'. Expected type: %s.",
                        ife.getValue(),
                        field,
                        targetType.getSimpleName()
                );
            }
        } else if (cause instanceof JsonParseException) {
            message = "Malformed JSON request.";
        } else {
            message = "Request body is invalid or cannot be parsed.";
        }
        return message;
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
                .body(ErrorResponse.builder()
                        .timestamp(Instant.now())
                        .status(status.value())
                        .error(error)
                        .message(message)
                        .path(request.getRequestURI())
                        .validationErrors(validationErrors)
                        .build());
    }
}
