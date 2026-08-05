package com.healthcare.aiservice.exception.rest_exception;

import com.healthcare.aiservice.common.prompt.model.AiPromptKey;
import com.healthcare.aiservice.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class AiPromptStateInvalidException extends RestException {

    private static final HttpStatus STATUS = HttpStatus.CONFLICT;

    public AiPromptStateInvalidException(AiPromptKey key) {
        super(
                STATUS,
                String.format(
                        "Multiple active AI prompts found for feature '%s', type '%s', and target model '%s'.",
                        key.feature(),
                        key.type(),
                        key.targetModel()
                ),
                ErrorCode.AI_PROMPT_VERSION_CONFLICT
        );
    }
}