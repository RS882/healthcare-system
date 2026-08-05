package com.healthcare.aiservice.exception.rest_exception;

import com.healthcare.aiservice.common.prompt.model.AiPromptKey;
import com.healthcare.aiservice.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class AiPromptNotFoundException extends RestException {

    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;

    public AiPromptNotFoundException(String promptId) {

        super(STATUS,
                String.format("AI prompt with id: '%s' no found", promptId),
                ErrorCode.AI_PROMPT_NOT_FOUND);
    }

    public AiPromptNotFoundException(AiPromptKey aiPromptKey) {
        super(STATUS,
                String.format("AI prompt with feature : '%s', type: '%s' for provider model : '%s' no found",
                        aiPromptKey.feature(),
                        aiPromptKey.type(),
                        aiPromptKey.targetModel()),
                ErrorCode.AI_PROMPT_NOT_FOUND);
    }
}
