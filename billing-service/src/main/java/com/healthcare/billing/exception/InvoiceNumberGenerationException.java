package com.healthcare.billing.exception;

import com.healthcare.billing.exception.error_code.ErrorCode;
import org.springframework.http.HttpStatus;

public class InvoiceNumberGenerationException extends RestException {

    private static final HttpStatus STATUS = HttpStatus.INTERNAL_SERVER_ERROR;

    public InvoiceNumberGenerationException(String message) {
        super(
                STATUS,
                message,
                ErrorCode.INVOICE_NUMBER_GENERATION_ERROR
        );
    }
}