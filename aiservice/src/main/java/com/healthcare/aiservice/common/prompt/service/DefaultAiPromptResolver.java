package com.healthcare.aiservice.common.prompt.service;

import com.healthcare.aiservice.common.prompt.model.AiPrompt;
import com.healthcare.aiservice.common.prompt.model.AiPromptKey;
import com.healthcare.aiservice.config.constant.PromptSource;
import com.healthcare.aiservice.common.prompt.model.ResolvedPrompt;
import com.healthcare.aiservice.common.prompt.service.interfaces.AiPromptResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class DefaultAiPromptResolver implements AiPromptResolver {

    private final CachedActivePromptService activePromptService;

    @Override
    public ResolvedPrompt resolvePrompt(
            AiPromptKey key,
            Supplier<String> fallbackPromptSupplier
    ) {
        AiPrompt activePrompt =
                activePromptService.findActivePrompt(key);

        if (isActivePromptValid(activePrompt)) {
            return new ResolvedPrompt(
                    key,
                    PromptSource.DATABASE,
                    activePrompt.version(),
                    activePrompt.content()
            );
        }

        return new ResolvedPrompt(
                key,
                PromptSource.FALLBACK,
                null,
                fallbackPromptSupplier.get()
        );
    }

    private boolean isActivePromptValid(AiPrompt activePrompt) {
        return activePrompt != null
                && StringUtils.hasText(activePrompt.content());
    }
}