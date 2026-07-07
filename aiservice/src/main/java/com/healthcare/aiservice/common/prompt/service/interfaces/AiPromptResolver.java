package com.healthcare.aiservice.common.prompt.service.interfaces;

import com.healthcare.aiservice.common.prompt.model.AiPromptKey;
import com.healthcare.aiservice.common.prompt.model.ResolvedPrompt;

import java.util.function.Supplier;

public interface AiPromptResolver {

    ResolvedPrompt resolvePrompt(
            AiPromptKey key,
            Supplier<String> fallbackPromptSupplier
    );
}