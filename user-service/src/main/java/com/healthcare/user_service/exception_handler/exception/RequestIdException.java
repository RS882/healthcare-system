package com.healthcare.user_service.exception_handler.exception;

import org.springframework.http.HttpStatus;

public class RequestIdException extends RestException {

    private final HttpStatus status;

    public RequestIdException(HttpStatus status, String message) {
        super(status, message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
