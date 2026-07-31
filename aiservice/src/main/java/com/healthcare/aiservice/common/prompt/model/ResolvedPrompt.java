package com.healthcare.aiservice.common.prompt.model;

import com.healthcare.aiservice.config.constant.PromptSource;

public record ResolvedPrompt(
        AiPromptKey key,

        PromptSource source,

        Long version,

        String content

) {
}
