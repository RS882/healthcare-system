package com.healthcare.billing.exception;

import com.healthcare.billing.exception.error_code.ErrorCode;
import org.springframework.http.HttpStatus;

public class InvalidPersistedCurrencyException extends RestException {

    private static final HttpStatus STATUS =
            HttpStatus.INTERNAL_SERVER_ERROR;

    public InvalidPersistedCurrencyException(String currencyCode) {
        super(
                STATUS,
                "Invalid persisted currency code: '%s'".formatted(currencyCode),
                ErrorCode.INVALID_PERSISTED_CURRENCY
        );
    }
}