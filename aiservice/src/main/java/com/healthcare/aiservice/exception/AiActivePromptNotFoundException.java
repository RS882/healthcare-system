package com.healthcare.aiservice.exception;

import com.healthcare.aiservice.common.prompt.model.AiPromptKey;
import org.springframework.http.HttpStatus;

public class AiActivePromptNotFoundException extends RestException {

    private static final HttpStatus status = HttpStatus.NOT_FOUND;

    public AiActivePromptNotFoundException(String promptId) {

        super(status, String.format("Active AI prompt with id: '%s' no found", promptId));
    }

    public AiActivePromptNotFoundException(AiPromptKey aiPromptKey) {
        super(status,
                String.format("Active AI prompt with feature : '%s', type: '%s' for provider model :'%s' no found",
                        aiPromptKey.feature(),
                        aiPromptKey.type(),
                        aiPromptKey.targetModel()));
    }
}
