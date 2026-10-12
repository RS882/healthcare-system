package com.healthcare.billing.exception;

import com.healthcare.billing.exception.error_code.ErrorCode;
import org.springframework.http.HttpStatus;

public class BankTransferValidationException extends RestException {

    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public BankTransferValidationException(String message) {
        super(
                STATUS,
                message,
                ErrorCode.BANK_TRANSFER_VALIDATION_ERROR
        );
    }
}