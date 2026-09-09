package com.healthcare.billing.exception;

import com.healthcare.billing.exception.error_code.ErrorCode;
import org.springframework.http.HttpStatus;

public class MedicalServiceProviderException extends RestException {

    private static final HttpStatus STATUS = HttpStatus.INTERNAL_SERVER_ERROR;

    public MedicalServiceProviderException(String message) {
        super(
                STATUS,
                message,
                ErrorCode.MEDICAL_SERVICE_PROVIDER_ERROR
        );
    }
}