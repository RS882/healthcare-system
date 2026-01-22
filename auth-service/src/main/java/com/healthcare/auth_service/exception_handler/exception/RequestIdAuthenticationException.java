package com.healthcare.auth_service.exception_handler.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;

public class RequestIdAuthenticationException extends AuthenticationException {

    private final HttpStatus status;

    public RequestIdAuthenticationException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
