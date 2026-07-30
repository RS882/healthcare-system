package com.healthcare.aiservice.common.prompt.service;


import com.healthcare.aiservice.common.prompt.model.AiPrompt;
import com.healthcare.aiservice.common.prompt.model.AiPromptKey;
import com.healthcare.aiservice.common.prompt.cache.CacheNames;
import com.healthcare.aiservice.exception.AiPromptStateInvalidException;
import com.healthcare.aiservice.repository.AiPromptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CachedActivePromptService {

    private final AiPromptRepository repository;

    @Cacheable(
            cacheNames = CacheNames.ACTIVE_PROMPTS,
            key = "T(com.healthcare.aiservice.common.prompt.cache.PromptCacheKey).of(#key)",
            unless = "#result == null",
            sync = true
    )
    public AiPrompt findActivePrompt(AiPromptKey key) {
        List<AiPrompt> activePrompts =
                repository.findAllByFeatureAndTypeAndTargetModelAndActiveTrue(
                        key.feature(),
                        key.type(),
                        key.targetModel()
                );

        if (activePrompts.size() > 1) {
            throw new AiPromptStateInvalidException(key);
        }

        if (activePrompts.size() == 1) {
            AiPrompt activePrompt = activePrompts.get(0);

            if (StringUtils.hasText(activePrompt.content())) {
                return activePrompt;
            }
        }

        return null;
    }
}
