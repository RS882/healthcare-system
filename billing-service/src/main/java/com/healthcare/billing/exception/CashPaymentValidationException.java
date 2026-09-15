package com.healthcare.billing.exception;

import com.healthcare.billing.exception.error_code.ErrorCode;
import org.springframework.http.HttpStatus;

public class CashPaymentValidationException extends RestException {

    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public CashPaymentValidationException(String message) {
        super(
                STATUS,
                message,
                ErrorCode.CASH_PAYMENT_VALIDATION_ERROR
        );
    }
}