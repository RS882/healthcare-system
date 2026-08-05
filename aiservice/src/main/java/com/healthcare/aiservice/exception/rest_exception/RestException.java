package com.healthcare.aiservice.exception.rest_exception;

import com.healthcare.aiservice.exception.ErrorCode;
import com.healthcare.aiservice.exception.dto.ErrorResponse;
import com.healthcare.aiservice.exception.dto.ValidationError;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Set;

@Getter
public abstract class RestException extends RuntimeException {
    private final HttpStatus status;
    private final ErrorResponse response;

    protected RestException(
            HttpStatus status,
            String message) {
        this(status, message, null, null, null);
    }

    protected RestException(
            HttpStatus status,
            String message,
            ErrorCode errorCode) {
        this(status, message, errorCode, null, null);
    }

    protected RestException(
            HttpStatus status,
            String message,
            ErrorCode errorCode,
            Throwable cause) {
        this(status, message, errorCode, cause, null);
    }

    protected RestException(
            HttpStatus status,
            String message,
            ErrorCode errorCode,
            Set<ValidationError> errors) {
        this(status, message, errorCode, null, errors);
    }

    protected RestException(
            HttpStatus status,
            String message,
            ErrorCode errorCode,
            Throwable cause,
            Set<ValidationError> errors) {
        super(String.join(";\n", message), cause);
        this.status = status;
        this.response = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(errorCode == null ?
                        status.getReasonPhrase() :
                        errorCode.name())
                .message(message)
                .validationErrors(errors)
                .build();
    }
}
