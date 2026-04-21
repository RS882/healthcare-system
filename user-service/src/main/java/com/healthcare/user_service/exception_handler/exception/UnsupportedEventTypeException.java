package com.healthcare.user_service.exception_handler.exception;

import org.springframework.http.HttpStatus;

public class UnsupportedEventTypeException extends RestException {
    public UnsupportedEventTypeException(String eventTypeName) {
        super(HttpStatus.BAD_REQUEST, "Unsupported event type: " + eventTypeName);
    }
}
