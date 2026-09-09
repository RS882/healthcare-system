package com.healthcare.billing.exception;

import com.healthcare.billing.exception.error_code.ErrorCode;
import org.springframework.http.HttpStatus;

public class InvoiceNotFoundException extends RestException {

    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;

    public InvoiceNotFoundException(Long invoiceId) {
        super(
                STATUS,
                "Invoice with id %d not found".formatted(invoiceId),
                ErrorCode.INVOICE_NOT_FOUND
        );
    }

    public InvoiceNotFoundException(String invoiceNumber) {
        super(
                STATUS,
                "Invoice with number %s not found".formatted(invoiceNumber),
                ErrorCode.INVOICE_NOT_FOUND
        );
    }
}
