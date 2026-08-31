package com.healthcare.aiservice.exception.rest_exception;


import com.healthcare.aiservice.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class InternalRequestGrantUnavailableException
        extends RestException {

    private static final HttpStatus STATUS = HttpStatus.SERVICE_UNAVAILABLE;

    private static final ErrorCode ERROR_CODE = ErrorCode.INTERNAL_SERVER_ERROR;

    private static final String DEFAULT_MESSAGE =
            "Internal request grant service is unavailable";

    public InternalRequestGrantUnavailableException() {
        super(STATUS, DEFAULT_MESSAGE, ERROR_CODE);
    }

    public InternalRequestGrantUnavailableException(
            String message
    ) {
        super(STATUS, message, ERROR_CODE);
    }

    public InternalRequestGrantUnavailableException(
            Throwable cause
    ) {
        super(STATUS, DEFAULT_MESSAGE, ERROR_CODE, cause);
    }

    public InternalRequestGrantUnavailableException(
            String message,
            Throwable cause
    ) {
        super(STATUS, message,ERROR_CODE, cause);
    }
}
