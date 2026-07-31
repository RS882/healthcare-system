package com.healthcare.aiservice.common.prompt.event;

import com.healthcare.aiservice.common.prompt.model.AiPromptKey;
import com.healthcare.aiservice.common.prompt.service.PromptCacheEvictionService;
import com.healthcare.aiservice.config.constant.AiProviderModel;
import com.healthcare.aiservice.config.constant.FeatureName;
import com.healthcare.aiservice.config.constant.PromptType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Active prompt changed event listener tests: ")
class ActivePromptChangedEventListenerTest {

    private static final AiPromptKey PROMPT_KEY =
            new AiPromptKey(
                    FeatureName.MEDICAL_SUMMARY,
                    PromptType.SYSTEM,
                    AiProviderModel.LLAMA_3
            );

    @Mock
    private PromptCacheEvictionService cacheEvictionService;

    @InjectMocks
    private ActivePromptChangedEventListener listener;

    @Test
    void handle_ShouldEvictActivePromptCacheEntry() {
        ActivePromptChangedEvent event =
                new ActivePromptChangedEvent(PROMPT_KEY);

        listener.handle(event);

        verify(cacheEvictionService)
                .evictIfPresent(PROMPT_KEY);

        verifyNoMoreInteractions(cacheEvictionService);
    }
}