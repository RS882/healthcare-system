package com.healthcare.billing.exception;

import com.healthcare.billing.exception.error_code.ErrorCode;
import org.springframework.http.HttpStatus;

public class CardPaymentServerUnavailableException extends RestException {

    private static final HttpStatus STATUS = HttpStatus.SERVICE_UNAVAILABLE;

    public CardPaymentServerUnavailableException(String message) {
        super(
                STATUS,
                message,
                ErrorCode.SERVICE_UNAVAILABLE
        );
    }
}