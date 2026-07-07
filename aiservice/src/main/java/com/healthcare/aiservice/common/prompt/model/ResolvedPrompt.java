package com.healthcare.aiservice.common.prompt.model;

public record ResolvedPrompt(
        AiPromptKey key,

        PromptSource source,

        Long version,

        String content

) {
}
