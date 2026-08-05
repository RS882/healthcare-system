package com.healthcare.aiservice.exception.rest_exception;

import com.healthcare.aiservice.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class RequestIdSaveException extends RestException {

    private static final HttpStatus STATUS = HttpStatus.SERVICE_UNAVAILABLE;
    public RequestIdSaveException() {

        super(
                STATUS,
                "Request Id isn`t save",
                ErrorCode.SERVICE_UNAVAILABLE);
    }
}
