package com.healthcare.billing.exception;

import com.healthcare.billing.exception.error_code.ErrorCode;
import org.springframework.http.HttpStatus;

public class InvalidBillingCurrencyException extends RestException {

    private static final HttpStatus STATUS =
            HttpStatus.INTERNAL_SERVER_ERROR;

    public InvalidBillingCurrencyException(String currencyCode) {
        super(
                STATUS,
                "Invalid billing currency configuration: '%s'"
                        .formatted(currencyCode),
                ErrorCode.INVALID_BILLING_CURRENCY
        );
    }
}