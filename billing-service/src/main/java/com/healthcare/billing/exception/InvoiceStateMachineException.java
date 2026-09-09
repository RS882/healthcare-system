package com.healthcare.billing.exception;

import com.healthcare.billing.exception.error_code.ErrorCode;
import org.springframework.http.HttpStatus;

public class InvoiceStateMachineException extends RestException {

    private static final HttpStatus STATUS = HttpStatus.INTERNAL_SERVER_ERROR;

    public InvoiceStateMachineException(String message) {
        super(
                STATUS,
                message,
                ErrorCode.INVOICE_STATE_MACHINE_ERROR
        );
    }
}