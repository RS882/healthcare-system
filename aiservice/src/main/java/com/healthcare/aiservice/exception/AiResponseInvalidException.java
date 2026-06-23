package com.healthcare.aiservice.exception;

import org.springframework.http.HttpStatus;

public class AiResponseInvalidException extends RestException {

    public AiResponseInvalidException( String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}
