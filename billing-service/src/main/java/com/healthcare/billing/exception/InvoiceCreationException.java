package com.healthcare.billing.exception;

import com.healthcare.billing.exception.error_code.ErrorCode;
import org.springframework.http.HttpStatus;

public class InvoiceCreationException extends RestException {

    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public InvoiceCreationException(String message) {
        super(
                STATUS,
                message,
                ErrorCode.INVOICE_CREATION_ERROR
        );
    }
}