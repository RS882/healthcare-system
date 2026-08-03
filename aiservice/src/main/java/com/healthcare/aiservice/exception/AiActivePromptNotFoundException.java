package com.healthcare.aiservice.exception;

import com.healthcare.aiservice.common.prompt.model.AiPromptKey;
import org.springframework.http.HttpStatus;

public class AiActivePromptNotFoundException extends RestException {

    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;

    public AiActivePromptNotFoundException(String promptId) {

        super(STATUS,
                String.format("Active AI prompt with id: '%s' no found", promptId),
                ErrorCode.AI_PROMPT_NOT_FOUND);
    }

    public AiActivePromptNotFoundException(AiPromptKey aiPromptKey) {
        super(STATUS,
                String.format("Active AI prompt with feature : '%s', type: '%s' for provider model :'%s' no found",
                        aiPromptKey.feature(),
                        aiPromptKey.type(),
                        aiPromptKey.targetModel()),
                ErrorCode.AI_PROMPT_NOT_FOUND);
    }
}
