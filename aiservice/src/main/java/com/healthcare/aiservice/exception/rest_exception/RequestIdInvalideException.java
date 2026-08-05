package com.healthcare.aiservice.exception.rest_exception;

import org.springframework.http.HttpStatus;

public class RequestIdInvalideException extends RestException {

    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public RequestIdInvalideException(String message) {
        super(STATUS, message);

    }

    public HttpStatus getStatus() {
        return STATUS;
    }
}
