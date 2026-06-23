package com.healthcare.aiservice.exception;

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

    protected RestException(HttpStatus status, String message) {
        this(status, message, null, null);
    }

    protected RestException(HttpStatus status, String message, Throwable cause) {
        this(status, message, cause, null);
    }

    protected RestException(HttpStatus status, String message, Set<ValidationError> errors) {
        this(status, message, null, errors);
    }

    protected RestException(HttpStatus status, String message, Throwable cause, Set<ValidationError> errors) {
        super(String.join(";\n", message), cause);
        this.status = status;
        this.response = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .validationErrors(errors)
                .build();
    }
}
