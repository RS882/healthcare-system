package com.healthcare.user_service.exception_handler.exception;


import com.healthcare.user_service.exception_handler.dto.ErrorResponse;
import com.healthcare.user_service.exception_handler.dto.ValidationError;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
public abstract class RestException extends RuntimeException {
    private final HttpStatus status;
    private final ErrorResponse response;

    protected  RestException(HttpStatus status, String message) {
        this(status, Set.of(message), null, null);
    }

    protected  RestException(HttpStatus status, String message, Throwable cause) {
        this(status, Set.of(message), cause, null);
    }

    protected  RestException(HttpStatus status, String message, Set<ValidationError> errors) {
        this(status, Set.of(message), null, errors);
    }

    protected  RestException(HttpStatus status, Set<String> messages, Throwable cause, Set<ValidationError> errors) {
        super(String.join(";\n", messages), cause);
        this.status = status;
        this.response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(messages)
                .validationErrors(errors)
                .build();
    }
}
