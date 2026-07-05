package com.healthcare.aiservice.common.prompt.service;

import com.healthcare.aiservice.common.prompt.model.AiPrompt;
import com.healthcare.aiservice.common.prompt.model.AiPromptKey;
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
    public String resolvePrompt(
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
            return activePrompts.get(0).content();
        }

        return fallbackPromptSupplier.get();
    }
}
