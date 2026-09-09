package com.healthcare.billing.exception;

import com.healthcare.billing.exception.error_code.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.List;

public class MedicalServiceNotFoundException extends RestException {

    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;

    public MedicalServiceNotFoundException(List<Long> serviceIds) {
        super(
                STATUS,
                "Medical services not found: %s".formatted(serviceIds),
                ErrorCode.MEDICAL_SERVICE_NOT_FOUND
        );
    }

    public MedicalServiceNotFoundException(Long serviceId) {
        super(
                STATUS,
                "Medical services not found: %d".formatted(serviceId),
                ErrorCode.MEDICAL_SERVICE_NOT_FOUND
        );
    }
}