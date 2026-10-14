package com.healthcare.billing.exception;

import com.healthcare.billing.exception.error_code.ErrorCode;
import org.springframework.http.HttpStatus;

public class DispatcherValidationException extends RestException {

    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public DispatcherValidationException(String message) {
        super(
                STATUS,
                message,
                ErrorCode.DISPATCHER_VALIDATION_ERROR
        );
    }
}