package com.healthcare.auth_service.exception_handler.exception;

import org.springframework.http.HttpStatus;

public class RequestIdSaveException extends RestException{
    public RequestIdSaveException() {
        super(HttpStatus.SERVICE_UNAVAILABLE, "Request Id isn`t save");
    }
}
