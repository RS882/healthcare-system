package com.healthcare.aiservice.common.prompt.service.interfaces;

import com.healthcare.aiservice.common.prompt.model.AiPromptKey;

import java.util.function.Supplier;

public interface AiPromptResolver {

    String resolvePrompt(
            AiPromptKey key,
            Supplier<String> fallbackPromptSupplier
    );
}