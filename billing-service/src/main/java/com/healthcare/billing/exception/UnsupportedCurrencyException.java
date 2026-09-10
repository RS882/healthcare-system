package com.healthcare.billing.exception;

import com.healthcare.billing.exception.error_code.ErrorCode;
import org.springframework.http.HttpStatus;

public class UnsupportedCurrencyException extends RestException {

    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public UnsupportedCurrencyException(String name) {
        super(
                STATUS,
                "Currency is not supported: %s".formatted(name),
                ErrorCode.CURRENCY_NOMINAL_INDEX_ERROR
        );
    }
}
