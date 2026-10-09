package com.healthcare.billing.exception;

import com.healthcare.billing.exception.error_code.ErrorCode;
import org.springframework.http.HttpStatus;

public class PaymentBalanceValidationException extends RestException {

    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public PaymentBalanceValidationException(String message) {
        super(
                STATUS,
                message,
                ErrorCode.PAYMENT_BALANCE_VALIDATION_ERROR
        );
    }
}
