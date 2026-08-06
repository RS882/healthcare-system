package com.healthcare.auth_service.exception_handler.exception;


import org.springframework.http.HttpStatus;

public class InternalRequestGrantCreationException
        extends RestException {

    private static final HttpStatus STATUS = HttpStatus.INTERNAL_SERVER_ERROR;

    private static final String DEFAULT_MESSAGE =
            "Failed to create internal request grant";

    public InternalRequestGrantCreationException() {
        super(STATUS, DEFAULT_MESSAGE);
    }

    public InternalRequestGrantCreationException(
            String message
    ) {
        super(STATUS, message);
    }

    public InternalRequestGrantCreationException(
            Throwable cause
    ) {
        super(STATUS, DEFAULT_MESSAGE, cause);
    }

    public InternalRequestGrantCreationException(
            String message,
            Throwable cause
    ) {
        super(STATUS, message, cause);
    }
}