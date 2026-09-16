package com.healthcare.billing.exception;

import com.healthcare.billing.exception.error_code.ErrorCode;
import org.springframework.http.HttpStatus;

public class PaymentMapperException extends RestException {

    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public PaymentMapperException(String message) {
        super(
                STATUS,
                message,
                ErrorCode.PAYMENT_MAPPER_ERROR
        );
    }
}
