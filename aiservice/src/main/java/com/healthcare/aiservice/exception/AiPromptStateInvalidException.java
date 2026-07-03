package com.healthcare.aiservice.exception;

import com.healthcare.aiservice.common.prompt.model.AiPromptKey;
import org.springframework.http.HttpStatus;

public class AiPromptStateInvalidException extends RestException {

    private static final HttpStatus status = HttpStatus.NOT_FOUND;

    public AiPromptStateInvalidException(AiPromptKey aiPromptKey) {
        super(status,
                String.format("There are several AI prompts with with feature : '%s', type: '%s' for provider model : '%s' no found",
                        aiPromptKey.feature(),
                        aiPromptKey.type(),
                        aiPromptKey.targetModel()));
    }
}
