package com.healthcare.billing.exception;

import com.healthcare.billing.exception.error_code.ErrorCode;
import org.springframework.http.HttpStatus;

public class PaymentJpaAdapterException extends RestException {

    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public PaymentJpaAdapterException(String message) {
        super(
                STATUS,
                message,
                ErrorCode.PAYMENT_JPA_ADAPTER_ERROR
        );
    }
}
