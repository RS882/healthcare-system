package com.healthcare.aiservice.exception;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.healthcare.aiservice.exception.dto.ErrorResponse;
import com.healthcare.aiservice.exception.dto.ValidationError;
import jakarta.servlet.http.HttpServletRequest;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

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

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                "Validation failed",
                request.getRequestURI(),
                validationErrors
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ErrorResponse> handleRequestParameterException(
            Exception ex,
            HttpServletRequest request
    ) {
        String message;

        if (ex instanceof MethodArgumentTypeMismatchException mismatch) {
            message = buildTypeMismatchMessage(mismatch);

        } else if (ex instanceof MissingServletRequestParameterException missing) {
            message = buildMissingParameterMessage(missing);

        } else {
            message = "Invalid request parameter";
        }

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.name(),
                message,
                request.getRequestURI(),
                Set.of()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
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

    @ExceptionHandler(NonTransientAiException.class)
    public ResponseEntity<ErrorResponse> handleAiProviderException(
            NonTransientAiException ex,
            HttpServletRequest request
    ) {
        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                HttpStatus.BAD_GATEWAY.value(),
                "AI_PROVIDER_ERROR",
                "AI provider failed to process the request",
                request.getRequestURI(),
                Set.of()
        );

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ErrorResponse> handleAiConnectionException(
            ResourceAccessException ex,
            HttpServletRequest request
    ) {
        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "AI_PROVIDER_UNAVAILABLE",
                "AI provider is unavailable",
                request.getRequestURI(),
                Set.of()
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @ExceptionHandler(MismatchedInputException.class)
    public ResponseEntity<ErrorResponse> handleAiResponseParsingException(
            MismatchedInputException ex,
            HttpServletRequest request
    ) {

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                HttpStatus.BAD_GATEWAY.value(),
                "AI_RESPONSE_PARSING_ERROR",
                "AI returned response in an unexpected format",
                request.getRequestURI(),
                Set.of()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        log.warn(
                "Invalid request body. URI: {}, reason: {}",
                request.getRequestURI(),
                buildRequestBodyErrorMessage(ex)
        );

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.name())
                .message(buildRequestBodyErrorMessage(ex))
                .path(request.getRequestURI())
                .validationErrors(Set.of())
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
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

    @ExceptionHandler(AiResponseParsingException.class)
    public ResponseEntity<ErrorResponse> handleAiResponseParsingException(
            AiResponseParsingException ex,
            HttpServletRequest request
    ) {
        log.error(
                "AI response parsing failed. path={}, rawResponse={}, extractedJson={}",
                request.getRequestURI(),
                ex.getRawResponse(),
                ex.getExtractedJson(),
                ex
        );

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_GATEWAY.value())
                .error(HttpStatus.BAD_GATEWAY.name())
                .message("AI provider returned invalid response format")
                .path(request.getRequestURI())
                .validationErrors(Set.of())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorResponse);
    }

    @ExceptionHandler(RestException.class)
    public ResponseEntity<ErrorResponse> handleException(RestException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ex.getResponse();

        ErrorResponse.builder().path(request.getRequestURI()).build();

        log.error("REST Error: {}", errorResponse, ex);

        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unexpected error on path={}", request.getRequestURI(), ex);

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "Unexpected internal server error",
                request.getRequestURI(),
                Set.of()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
