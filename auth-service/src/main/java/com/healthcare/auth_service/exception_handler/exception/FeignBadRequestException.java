package com.healthcare.auth_service.exception_handler.exception;

import org.springframework.http.HttpStatus;

public class FeignBadRequestException extends RestException {

    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public FeignBadRequestException(String message) {
        super(STATUS, message);
    }

    public FeignBadRequestException(String message, Throwable cause) {
        super(STATUS, message, cause);
    }
}
