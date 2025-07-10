package com.healthcare.user_service.exception_handler.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends RestException {
    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }

    public NotFoundException( String message, Throwable cause) {
        super(HttpStatus.NOT_FOUND, message, cause);
    }
}
