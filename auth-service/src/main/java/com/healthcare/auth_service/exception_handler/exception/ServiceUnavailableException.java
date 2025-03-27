package com.healthcare.auth_service.exception_handler.exception;

import org.springframework.http.HttpStatus;

public class ServiceUnavailableException extends RestException {
    public ServiceUnavailableException(String message) {
        super(HttpStatus.SERVICE_UNAVAILABLE, message);
    }

    public ServiceUnavailableException(String message, Throwable cause) {
        super(HttpStatus.SERVICE_UNAVAILABLE, message, cause);
    }
}
