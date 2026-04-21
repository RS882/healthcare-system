package com.healthcare.user_service.exception_handler.exception;

import org.springframework.http.HttpStatus;

public class SerializeEventException extends RestException {
    public SerializeEventException(String eventTypeName, Throwable cause) {
        super(HttpStatus.BAD_REQUEST, "Failed to serialize event: " + eventTypeName, cause);
    }
}
