package com.healthcare.aiservice.exception;


import org.springframework.security.authentication.AuthenticationServiceException;

public class UserServiceUnavailableException
        extends AuthenticationServiceException {

    public UserServiceUnavailableException(
            String message
    ) {
        super(message);
    }

    public UserServiceUnavailableException(
            String message,
            Throwable cause
    ) {
        super(
                message,
                cause
        );
    }
}
