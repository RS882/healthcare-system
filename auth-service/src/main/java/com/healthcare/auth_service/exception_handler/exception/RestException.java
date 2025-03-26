package com.healthcare.auth_service.exception_handler.exception;

import com.healthcare.auth_service.exception_handler.dto.ErrorResponse;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
public class RestException extends RuntimeException {
    private final HttpStatus status;

    private final ErrorResponse response;

    public RestException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.response =  ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .build();
    }
}
