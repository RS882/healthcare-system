package com.healthcare.aiservice.exception.rest_exception;

import org.springframework.http.HttpStatus;

public class JsonExtractorException extends RestException {

    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;

    public JsonExtractorException(String message) {
        super(STATUS, message);    }

}
