package com.healthcare.billing.exception;

import com.healthcare.billing.exception.error_code.ErrorCode;
import org.springframework.http.HttpStatus;

public class CurrencyNominalIndexException extends RestException {

    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public CurrencyNominalIndexException(int i) {
        super(
                STATUS,
                "Nominal index out of bounds: %d".formatted(i),
                ErrorCode.CURRENCY_NOMINAL_INDEX_ERROR
        );
    }
}
