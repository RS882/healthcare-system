package com.healthcare.aiservice.common.prompt.service;

import com.healthcare.aiservice.common.prompt.cache.CacheNames;
import com.healthcare.aiservice.common.prompt.cache.PromptCacheKey;
import com.healthcare.aiservice.common.prompt.mapper.AiPromptMapper;
import com.healthcare.aiservice.common.prompt.model.AiPrompt;
import com.healthcare.aiservice.common.prompt.model.AiPromptKey;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PromptCacheEvictionService {

    private final CacheManager cacheManager;

    public boolean evictIfPresent(AiPrompt prompt) {
        return evictIfPresent(AiPromptMapper.toKey(prompt));
    }

    public boolean evictIfPresent(AiPromptKey key) {
        Cache cache = cacheManager.getCache(CacheNames.ACTIVE_PROMPTS);

        if (cache == null) {
            return false;
        }

       return cache.evictIfPresent(PromptCacheKey.of(key));
    }
}