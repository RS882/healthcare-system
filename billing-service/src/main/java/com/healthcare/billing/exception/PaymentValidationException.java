package com.healthcare.billing.exception;

import com.healthcare.billing.exception.error_code.ErrorCode;
import org.springframework.http.HttpStatus;

public class PaymentValidationException extends RestException {

    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public PaymentValidationException(String message) {
        super(
                STATUS,
                message,
                ErrorCode.INVOICE_VALIDATION_ERROR
        );
    }
}
