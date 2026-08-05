package com.healthcare.aiservice.exception.rest_exception;

import com.healthcare.aiservice.common.dto.NoteBasedRequest;
import com.healthcare.aiservice.common.prompt.service.interfaces.PromptProvider;
import com.healthcare.aiservice.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class InvalidRequestTypeForFeatureException extends RestException {


    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public InvalidRequestTypeForFeatureException(
            PromptProvider<? extends NoteBasedRequest> provider,
            Class<?> currentRequestType) {

        super(STATUS,
                String.format("Invalid request type for feature '%s'. Expected '%s', got '%s'.",
                        provider.feature().name(),
                        provider.requestType().getSimpleName(),
                        currentRequestType.getSimpleName()),
                ErrorCode.INVALID_REQUEST_PARAMETER);
    }
}
