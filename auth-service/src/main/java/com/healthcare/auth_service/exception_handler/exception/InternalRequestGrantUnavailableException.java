package com.healthcare.auth_service.exception_handler.exception;


import org.springframework.http.HttpStatus;

public class InternalRequestGrantUnavailableException
        extends RestException {

    private static final HttpStatus STATUS = HttpStatus.SERVICE_UNAVAILABLE;

    private static final String DEFAULT_MESSAGE =
            "Internal request grant service is unavailable";

    public InternalRequestGrantUnavailableException() {
        super(STATUS, DEFAULT_MESSAGE);
    }

    public InternalRequestGrantUnavailableException(
            String message
    ) {
        super(STATUS, message);
    }

    public InternalRequestGrantUnavailableException(
            Throwable cause
    ) {
        super(STATUS, DEFAULT_MESSAGE, cause);
    }

    public InternalRequestGrantUnavailableException(
            String message,
            Throwable cause
    ) {
        super(STATUS, message, cause);
    }
}
