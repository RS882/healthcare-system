package com.healthcare.user_service.exception_handler.exception;


import org.springframework.security.authentication.BadCredentialsException;

public class InternalRequestGrantNotFoundException
        extends BadCredentialsException {

    private static final String DEFAULT_MESSAGE =
            "Internal request grant was not found, expired or already consumed";

    public InternalRequestGrantNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

    public InternalRequestGrantNotFoundException(
            Throwable cause
    ) {
        super(DEFAULT_MESSAGE, cause);
    }
}
