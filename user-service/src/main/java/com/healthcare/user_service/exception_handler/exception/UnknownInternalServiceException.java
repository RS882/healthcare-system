package com.healthcare.user_service.exception_handler.exception;

import org.springframework.http.HttpStatus;

public class UnknownInternalServiceException extends RestException {

    private static final HttpStatus status = HttpStatus.UNAUTHORIZED;

    public UnknownInternalServiceException(String serviceName) {

        super(status, "Unknown internal service: " + serviceName);
    }
}
