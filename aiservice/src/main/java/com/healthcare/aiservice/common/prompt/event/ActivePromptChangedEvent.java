package com.healthcare.aiservice.common.prompt.event;

import com.healthcare.aiservice.common.prompt.model.AiPromptKey;

public record ActivePromptChangedEvent(
        AiPromptKey key
) {
}
