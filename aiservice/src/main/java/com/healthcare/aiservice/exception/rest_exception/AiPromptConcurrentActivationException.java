package com.healthcare.aiservice.exception.rest_exception;


import com.healthcare.aiservice.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class AiPromptConcurrentActivationException
        extends RestException {

    private static final HttpStatus STATUS =
            HttpStatus.CONFLICT;

    public AiPromptConcurrentActivationException(
            String promptId
    ) {
        super(
                STATUS,
                "AI prompt '%s' could not be activated because another activation was performed concurrently. Please retry the request."
                        .formatted(promptId),
                ErrorCode.AI_PROMPT_VERSION_CONFLICT
        );
    }

    public AiPromptConcurrentActivationException(
            String promptId,
            Throwable cause
    ) {
        super(
                STATUS,
                "AI prompt '%s' could not be activated because another activation was performed concurrently. Please retry the request."
                        .formatted(promptId),
                ErrorCode.AI_PROMPT_VERSION_CONFLICT,
                cause
        );
    }
}
