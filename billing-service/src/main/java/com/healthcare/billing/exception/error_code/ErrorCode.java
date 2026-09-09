package com.healthcare.billing.exception.error_code;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    VALIDATION_ERROR("Validation failed"),

    INVOICE_NOT_FOUND("Invoice not found"),
    MEDICAL_SERVICE_NOT_FOUND("Medical service not found"),

    INVALID_INVOICE_TRANSITION("Invalid invoice state transition"),

    INVALID_REQUEST_PARAMETER("Invalid request parameter"),
    INVALID_REQUEST_BODY("Request body is invalid"),

    INVOICE_VALIDATION_ERROR("Invoice validation failed"),
    INVOICE_ITEM_VALIDATION_ERROR("Invoice item validation failed"),
    BILLING_VALIDATION_ERROR("Billing validation failed"),
    MONEY_VALIDATION_ERROR("Money validation failed"),
    CURRENCY_MISMATCH("Currency mismatch"),
    INVALID_BILLING_CURRENCY("Invalid billing currency configuration"),
    INVALID_PERSISTED_CURRENCY("Invalid persisted currency"),

    INVOICE_PERSISTENCE_MAPPING_ERROR("Invoice persistence mapping failed"),
    INVOICE_NUMBER_GENERATION_ERROR("Invoice number generation failed"),
    INVOICE_CREATION_ERROR("Invoice creation failed"),
    MEDICAL_SERVICE_PROVIDER_ERROR("Medical service provider failed"),
    INVOICE_STATE_MACHINE_ERROR("Invoice state machine failed"),

    INTERNAL_SERVER_ERROR("Unexpected internal server error"),
    SERVICE_UNAVAILABLE("Service unavailable");

    private final String defaultMessage;
}
