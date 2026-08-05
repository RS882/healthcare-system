package com.healthcare.aiservice.exception.rest_exception;

import com.healthcare.aiservice.common.prompt.model.AiPromptKey;
import com.healthcare.aiservice.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class AiPromptVersionConflictException extends RestException {

    private static final HttpStatus STATUS = HttpStatus.CONFLICT;

    public AiPromptVersionConflictException(
            AiPromptKey key,
            long version
    ) {
        super(
                STATUS,
                String.format(
                        "AI prompt version '%d' already exists for feature '%s', type '%s', and target model '%s'.",
                        version,
                        key.feature(),
                        key.type(),
                        key.targetModel()
                ),
                ErrorCode.AI_PROMPT_VERSION_CONFLICT
        );
    }
}
