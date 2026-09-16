package com.healthcare.billing.exception;

import com.healthcare.billing.exception.error_code.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Set;

public class PaymentReconstitutionException extends RestException {

    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public PaymentReconstitutionException(String message) {
        super(
                STATUS,
                message,
                ErrorCode.INVOICE_VALIDATION_ERROR
        );
    }

    public PaymentReconstitutionException(List<String> messages) {
        super(
                STATUS,
                messages.toString(),
                ErrorCode.PAYMENT_RECONSTITUTION_ERROR
        );
    }
}
