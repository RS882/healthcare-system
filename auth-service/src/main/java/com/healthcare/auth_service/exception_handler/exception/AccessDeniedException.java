package com.healthcare.auth_service.exception_handler.exception;

import org.springframework.http.HttpStatus;

public class AccessDeniedException extends RestException {
    public AccessDeniedException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }

    public AccessDeniedException( String message, Throwable cause) {
        super(HttpStatus.FORBIDDEN, message, cause);
    }
}
