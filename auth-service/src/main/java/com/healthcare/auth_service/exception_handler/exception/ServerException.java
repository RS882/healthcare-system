package com.healthcare.auth_service.exception_handler.exception;

import org.springframework.http.HttpStatus;

public class ServerException extends RestException {

    public ServerException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    public ServerException(String message, Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message, cause);
    }
}