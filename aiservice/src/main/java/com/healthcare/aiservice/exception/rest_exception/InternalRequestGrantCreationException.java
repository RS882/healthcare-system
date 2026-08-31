package com.healthcare.aiservice.exception.rest_exception;


import com.healthcare.aiservice.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class InternalRequestGrantCreationException
        extends RestException {

    private static final HttpStatus STATUS = HttpStatus.INTERNAL_SERVER_ERROR;

    private static final ErrorCode ERROR_CODE = ErrorCode.INTERNAL_SERVER_ERROR;

    private static final String DEFAULT_MESSAGE =
            "Failed to create internal request grant";

    public InternalRequestGrantCreationException() {
        super(STATUS, DEFAULT_MESSAGE,ERROR_CODE
        );
    }

    public InternalRequestGrantCreationException(
            String message
    ) {
        super(STATUS, message,ERROR_CODE);
    }

    public InternalRequestGrantCreationException(
            Throwable cause
    ) {
        super(STATUS, DEFAULT_MESSAGE, ERROR_CODE, cause);
    }

    public InternalRequestGrantCreationException(
            String message,
            Throwable cause
    ) {
        super(STATUS, message, ERROR_CODE, cause);
    }
}