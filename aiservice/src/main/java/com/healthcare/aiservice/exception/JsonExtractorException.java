package com.healthcare.aiservice.exception;

import org.springframework.http.HttpStatus;

public class JsonExtractorException extends RestException {

    private static final HttpStatus status = HttpStatus.NOT_FOUND;

    public JsonExtractorException(String message) {
        super(status, message);    }

}
