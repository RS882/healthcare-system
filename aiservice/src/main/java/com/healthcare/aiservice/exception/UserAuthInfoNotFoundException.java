package com.healthcare.aiservice.exception;

import org.springframework.security.authentication.BadCredentialsException;

public class UserAuthInfoNotFoundException
        extends BadCredentialsException {

    private static final String DEFAULT_MESSAGE =
            "Authentication information was not found";

    public UserAuthInfoNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

    public UserAuthInfoNotFoundException(
            String message
    ) {
        super(message);
    }

    public UserAuthInfoNotFoundException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }

    public UserAuthInfoNotFoundException(
            Throwable cause
    ) {
        super(DEFAULT_MESSAGE, cause);
    }
}