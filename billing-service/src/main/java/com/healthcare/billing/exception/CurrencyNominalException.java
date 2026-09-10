package com.healthcare.billing.exception;

import com.healthcare.billing.exception.error_code.ErrorCode;
import org.springframework.http.HttpStatus;

public class CurrencyNominalException extends RestException {

    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public CurrencyNominalException(String message) {
        super(
                STATUS,
                message,
                ErrorCode.CURRENCY_NOMINAL_ERROR
        );
    }
}
