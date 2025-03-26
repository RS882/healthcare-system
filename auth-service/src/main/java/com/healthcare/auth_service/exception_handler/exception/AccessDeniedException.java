package com.healthcare.auth_service.exception_handler.exception;

import org.springframework.http.HttpStatus;

public class AccessDeniedException extends RestException {
    public AccessDeniedException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
