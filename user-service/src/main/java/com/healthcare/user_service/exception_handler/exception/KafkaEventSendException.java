package com.healthcare.user_service.exception_handler.exception;

import org.springframework.http.HttpStatus;

public class KafkaEventSendException extends RestException {
    public KafkaEventSendException(String message) {
        super(HttpStatus.SERVICE_UNAVAILABLE, message);
    }

    public KafkaEventSendException(String message, Throwable cause) {
        super(HttpStatus.SERVICE_UNAVAILABLE, message, cause);
    }
}


