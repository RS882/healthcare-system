package com.healthcare.billing.exception;

import com.healthcare.billing.exception.error_code.ErrorCode;
import org.springframework.http.HttpStatus;

public class PaymentNotFoundException extends RestException {

    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;

    public PaymentNotFoundException(Long id) {
        super(
                STATUS,
                "Payment with id < %d > not found".formatted(id),
                ErrorCode.PAYMENT_NOT_FOUND
        );
    }
}
