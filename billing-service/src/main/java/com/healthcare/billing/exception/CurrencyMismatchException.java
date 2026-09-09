package com.healthcare.billing.exception;

import com.healthcare.billing.exception.error_code.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Currency;

public class CurrencyMismatchException extends RestException {

    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public CurrencyMismatchException(
            Currency expectedCurrency,
            Currency actualCurrency
    ) {
        super(
                STATUS,
                "Currency mismatch: expected %s, but was %s"
                        .formatted(
                                expectedCurrency.getCurrencyCode(),
                                actualCurrency.getCurrencyCode()
                        ),
                ErrorCode.CURRENCY_MISMATCH
        );
    }
}