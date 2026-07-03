package com.healthcare.aiservice.exception;

import com.healthcare.aiservice.common.prompt.model.AiPromptKey;
import org.springframework.http.HttpStatus;

public class AiPromptVersionConflictException extends RestException {

    private static final HttpStatus status = HttpStatus.CONFLICT;

    public AiPromptVersionConflictException(AiPromptKey aiPromptKey, long version) {
        super(status,
                String.format(
                        "AI prompt version '%d' already exists for feature '%s', type '%s' and target model '%s'.",
                        version,
                        aiPromptKey.feature(),
                        aiPromptKey.type(),
                        aiPromptKey.targetModel()
                ));
    }
}
