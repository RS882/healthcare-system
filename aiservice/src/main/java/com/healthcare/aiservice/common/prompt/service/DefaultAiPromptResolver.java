package com.healthcare.aiservice.common.prompt.service;

import com.healthcare.aiservice.common.prompt.model.AiPrompt;
import com.healthcare.aiservice.common.prompt.model.AiPromptKey;
import com.healthcare.aiservice.common.prompt.model.PromptSource;
import com.healthcare.aiservice.common.prompt.model.ResolvedPrompt;
import com.healthcare.aiservice.common.prompt.service.interfaces.AiPromptResolver;
import com.healthcare.aiservice.exception.AiPromptStateInvalidException;
import com.healthcare.aiservice.repository.AiPromptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class DefaultAiPromptResolver implements AiPromptResolver {

    private final AiPromptRepository repository;

    @Override
    public ResolvedPrompt resolvePrompt(
            AiPromptKey key,
            Supplier<String> fallbackPromptSupplier
    ) {
        List<AiPrompt> activePrompts =
                repository.findAllByFeatureAndTypeAndTargetModelAndActiveTrue(
                        key.feature(),
                        key.type(),
                        key.targetModel()
                );

        if (activePrompts.size() > 1) {
            throw new AiPromptStateInvalidException(key);
        }

        if (activePrompts.size() == 1
                && StringUtils.hasText(activePrompts.get(0).content())) {
            AiPrompt activePrompt = activePrompts.get(0);

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
}
