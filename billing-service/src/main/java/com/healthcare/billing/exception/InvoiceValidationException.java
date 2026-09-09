package com.healthcare.billing.exception;

import com.healthcare.billing.exception.error_code.ErrorCode;
import org.springframework.http.HttpStatus;

public class InvoiceValidationException extends RestException {

    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public InvoiceValidationException(String message) {
        super(
                STATUS,
                message,
                ErrorCode.INVOICE_VALIDATION_ERROR
        );
    }
}
