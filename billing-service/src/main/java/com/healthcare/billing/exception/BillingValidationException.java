package com.healthcare.billing.exception;

import com.healthcare.billing.exception.error_code.ErrorCode;
import org.springframework.http.HttpStatus;

public class BillingValidationException extends RestException {

    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public BillingValidationException(String message) {
        super(
                STATUS,
                message,
                ErrorCode.BILLING_VALIDATION_ERROR
        );
    }
}