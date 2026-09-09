package com.healthcare.billing.exception;

import com.healthcare.billing.exception.error_code.ErrorCode;
import org.springframework.http.HttpStatus;

public class MoneyValidationException extends RestException {

    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public MoneyValidationException(String message) {
        super(
                STATUS,
                message,
                ErrorCode.MONEY_VALIDATION_ERROR
        );
    }
}