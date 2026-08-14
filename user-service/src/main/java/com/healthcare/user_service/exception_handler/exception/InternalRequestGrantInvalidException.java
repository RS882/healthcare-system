package com.healthcare.user_service.exception_handler.exception;

import org.springframework.security.authentication.BadCredentialsException;

public class InternalRequestGrantInvalidException
        extends BadCredentialsException {

    public InternalRequestGrantInvalidException(
            String message
    ) {
        super(message);
    }

    public InternalRequestGrantInvalidException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}
