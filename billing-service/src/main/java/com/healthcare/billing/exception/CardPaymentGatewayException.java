package com.healthcare.billing.exception;

import com.healthcare.billing.exception.error_code.ErrorCode;
import org.springframework.http.HttpStatus;

public class CardPaymentGatewayException extends RestException {

    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public CardPaymentGatewayException(String message) {
        super(
                STATUS,
                message,
                ErrorCode.CARD_PAYMENT_GATEWAY_ERROR
        );
    }
}