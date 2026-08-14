package com.healthcare.user_service.exception_handler.exception;

import org.springframework.security.authentication.AuthenticationServiceException;

public class InternalRequestAuthenticationServiceException
        extends AuthenticationServiceException {

    public InternalRequestAuthenticationServiceException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}
