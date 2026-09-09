package com.healthcare.billing.exception;

import com.healthcare.billing.exception.error_code.ErrorCode;
import com.healthcare.billing.model.entity.invoice.enums.InvoiceEvent;
import com.healthcare.billing.model.entity.invoice.enums.InvoiceStatus;
import org.springframework.http.HttpStatus;

public class InvalidInvoiceTransitionException extends RestException {

    private static final HttpStatus STATUS = HttpStatus.CONFLICT;

    public InvalidInvoiceTransitionException(
            InvoiceStatus currentStatus,
            InvoiceEvent event
    ) {
        super(
                STATUS,
                "Transition from %s by event %s is not allowed".formatted(currentStatus, event),
                ErrorCode.INVALID_INVOICE_TRANSITION
        );
    }
}