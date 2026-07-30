package com.healthcare.aiservice.common.prompt.service;

import com.healthcare.aiservice.common.prompt.cache.CacheNames;
import com.healthcare.aiservice.common.prompt.model.AiPrompt;
import com.healthcare.aiservice.common.prompt.model.AiPromptKey;
import com.healthcare.aiservice.common.prompt.model.AiProviderModel;
import com.healthcare.aiservice.common.prompt.model.PromptType;
import com.healthcare.aiservice.config.constant.FeatureName;
import com.healthcare.aiservice.repository.AiPromptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringJUnitConfig
@Import({
        CachedActivePromptService.class,
        CachedActivePromptServiceCacheTest.CacheTestConfiguration.class
})
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Cached active prompt cache integration tests: ")
class CachedActivePromptServiceCacheTest {

    private static final AiPromptKey PROMPT_KEY = new AiPromptKey(
            FeatureName.MEDICAL_SUMMARY,
            PromptType.SYSTEM,
            AiProviderModel.LLAMA_3
    );

    @Autowired
    private CachedActivePromptService service;

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private AiPromptRepository repository;

    @BeforeEach
    void clearCache() {
        cacheManager.getCache(CacheNames.ACTIVE_PROMPTS).clear();
    }

    @Test
    void findActivePrompt_ShouldUseCache_OnSecondInvocation() {

        AiPrompt prompt = createPrompt();

        when(repository.findAllByFeatureAndTypeAndTargetModelAndActiveTrue(
                PROMPT_KEY.feature(),
                PROMPT_KEY.type(),
                PROMPT_KEY.targetModel()
        )).thenReturn(List.of(prompt));

        AiPrompt firstCall =
                service.findActivePrompt(PROMPT_KEY);

        AiPrompt secondCall =
                service.findActivePrompt(PROMPT_KEY);

        assertThat(firstCall).isNotNull();
        assertThat(secondCall).isNotNull();
        assertThat(secondCall).isEqualTo(firstCall);

        verify(repository, times(1))
                .findAllByFeatureAndTypeAndTargetModelAndActiveTrue(
                        PROMPT_KEY.feature(),
                        PROMPT_KEY.type(),
                        PROMPT_KEY.targetModel()
                );
    }

    @Test
    void findActivePrompt_ShouldNotCacheNullResult() {

        when(repository.findAllByFeatureAndTypeAndTargetModelAndActiveTrue(
                PROMPT_KEY.feature(),
                PROMPT_KEY.type(),
                PROMPT_KEY.targetModel()
        )).thenReturn(List.of());

        assertThat(service.findActivePrompt(PROMPT_KEY))
                .isNull();

        assertThat(service.findActivePrompt(PROMPT_KEY))
                .isNull();

        verify(repository, times(2))
                .findAllByFeatureAndTypeAndTargetModelAndActiveTrue(
                        PROMPT_KEY.feature(),
                        PROMPT_KEY.type(),
                        PROMPT_KEY.targetModel()
                );
    }

    private AiPrompt createPrompt() {
        return AiPrompt.builder()
                .id("prompt-id")
                .feature(PROMPT_KEY.feature())
                .type(PROMPT_KEY.type())
                .targetModel(PROMPT_KEY.targetModel())
                .version(1L)
                .content("Database prompt")
                .active(true)
                .build();
    }

    @TestConfiguration
    @EnableCaching
    static class CacheTestConfiguration {

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager(
                    CacheNames.ACTIVE_PROMPTS
            );
        }
    }
}
