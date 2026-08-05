package com.healthcare.aiservice.common.prompt.service;

import com.healthcare.aiservice.cache.PromptCacheKey;
import com.healthcare.aiservice.common.prompt.model.AiPromptKey;
import com.healthcare.aiservice.config.constant.AiProviderModel;
import com.healthcare.aiservice.config.constant.PromptType;
import com.healthcare.aiservice.config.constant.FeatureName;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static com.healthcare.aiservice.cache.CacheNames.ACTIVE_PROMPTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Prompt cache eviction service tests: ")
class PromptCacheEvictionServiceTest {

    private static final AiPromptKey PROMPT_KEY =
            new AiPromptKey(
                    FeatureName.MEDICAL_SUMMARY,
                    PromptType.SYSTEM,
                    AiProviderModel.LLAMA_3
            );

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @InjectMocks
    private PromptCacheEvictionService service;

    @Test
    void evictIfPresent_ShouldEvictPromptCacheEntry_WhenCacheExists() {
        String expectedCacheKey = PromptCacheKey.of(PROMPT_KEY);

        when(cacheManager.getCache(ACTIVE_PROMPTS))
                .thenReturn(cache);

        when(cache.evictIfPresent(expectedCacheKey))
                .thenReturn(true);

        boolean result = service.evictIfPresent(PROMPT_KEY);

        assertThat(result).isTrue();

        verify(cacheManager).getCache(ACTIVE_PROMPTS);
        verify(cache).evictIfPresent(expectedCacheKey);
    }

    @Test
    void evictIfPresent_ShouldReturnFalse_WhenCacheEntryDoesNotExist() {
        String expectedCacheKey = PromptCacheKey.of(PROMPT_KEY);

        when(cacheManager.getCache(ACTIVE_PROMPTS))
                .thenReturn(cache);

        when(cache.evictIfPresent(expectedCacheKey))
                .thenReturn(false);

        boolean result = service.evictIfPresent(PROMPT_KEY);

        assertThat(result).isFalse();

        verify(cache).evictIfPresent(expectedCacheKey);
    }

    @Test
    void evictIfPresent_ShouldReturnFalse_WhenCacheDoesNotExist() {
        when(cacheManager.getCache(ACTIVE_PROMPTS))
                .thenReturn(null);

        boolean result = service.evictIfPresent(PROMPT_KEY);

        assertThat(result).isFalse();

        verify(cacheManager).getCache(ACTIVE_PROMPTS);
        verifyNoInteractions(cache);
    }
}