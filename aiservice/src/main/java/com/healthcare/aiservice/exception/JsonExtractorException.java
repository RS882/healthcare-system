package com.healthcare.aiservice.exception;

import org.springframework.http.HttpStatus;

public class JsonExtractorException extends RestException {
    public JsonExtractorException(String message) {
        super(HttpStatus.BAD_REQUEST, message);    }

}
