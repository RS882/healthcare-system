package com.healthcare.billing.exception;

import com.healthcare.billing.exception.error_code.ErrorCode;
import org.springframework.http.HttpStatus;

public class PaymentDetailsValidationException extends RestException {

    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public PaymentDetailsValidationException(String message) {
        super(
                STATUS,
                message,
                ErrorCode.PAYMENT_DETAILS_VALIDATION_ERROR
        );
    }
}