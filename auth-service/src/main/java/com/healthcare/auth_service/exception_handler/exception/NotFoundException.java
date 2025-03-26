package com.healthcare.auth_service.exception_handler.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends RestException {
    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
