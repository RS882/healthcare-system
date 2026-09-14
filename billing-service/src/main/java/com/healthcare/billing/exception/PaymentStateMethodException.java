package com.healthcare.billing.exception;

import com.healthcare.billing.exception.error_code.ErrorCode;
import com.healthcare.billing.payment.model.enums.PaymentStatus;
import org.springframework.http.HttpStatus;

public class PaymentStateMethodException extends RestException {

    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public PaymentStateMethodException(String methodName, PaymentStatus status) {
        super(
                STATUS,
                "Method <%s> not available for %s".formatted(methodName, status.name()),
                ErrorCode.PAYMENT_STATE_METHOD_ERROR
        );
    }
}
