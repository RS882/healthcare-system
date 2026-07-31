package com.healthcare.aiservice.common.prompt.service;


import com.healthcare.aiservice.config.constant.AiProviderModel;
import com.healthcare.aiservice.config.constant.PromptSource;
import com.healthcare.aiservice.config.constant.PromptType;
import com.healthcare.aiservice.common.prompt.model.*;
import com.healthcare.aiservice.config.constant.FeatureName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Default AI prompt resolver tests: ")
class DefaultAiPromptResolverTest {

    private static final AiPromptKey PROMPT_KEY = new AiPromptKey(
            FeatureName.MEDICAL_SUMMARY,
            PromptType.SYSTEM,
            AiProviderModel.LLAMA_3
    );

    @Mock
    private CachedActivePromptService activePromptService;

    @Mock
    private Supplier<String> fallbackPromptSupplier;

    @InjectMocks
    private DefaultAiPromptResolver resolver;

    @Test
    void resolvePrompt_ShouldReturnDatabasePrompt_WhenSingleActivePromptExists() {
        AiPrompt activePrompt = createPrompt("prompt-id", 3L, "Database prompt");

        when(activePromptService.findActivePrompt(PROMPT_KEY))
                .thenReturn(activePrompt);

        ResolvedPrompt result = resolver.resolvePrompt(
                PROMPT_KEY,
                fallbackPromptSupplier
        );

        assertThat(result.key()).isEqualTo(PROMPT_KEY);
        assertThat(result.source()).isEqualTo(PromptSource.DATABASE);
        assertThat(result.version()).isEqualTo(3L);
        assertThat(result.content()).isEqualTo("Database prompt");

        verify(fallbackPromptSupplier, never()).get();
    }

    @Test
    void resolvePrompt_ShouldReturnFallbackPrompt_WhenNoActivePromptExists() {

        when(activePromptService.findActivePrompt(PROMPT_KEY))
                .thenReturn(null);

        when(fallbackPromptSupplier.get())
                .thenReturn("Fallback prompt");

        ResolvedPrompt result = resolver.resolvePrompt(
                PROMPT_KEY,
                fallbackPromptSupplier
        );

        assertThat(result.key()).isEqualTo(PROMPT_KEY);
        assertThat(result.source()).isEqualTo(PromptSource.FALLBACK);
        assertThat(result.version()).isNull();
        assertThat(result.content()).isEqualTo("Fallback prompt");

        verify(fallbackPromptSupplier).get();
    }

    @Test
    void resolvePrompt_ShouldReturnFallbackPrompt_WhenActivePromptContentIsBlank() {

        AiPrompt activePrompt = createPrompt("prompt-id", 3L, "     ");

        when(activePromptService.findActivePrompt(PROMPT_KEY))
                .thenReturn(activePrompt);

        when(fallbackPromptSupplier.get())
                .thenReturn("Fallback prompt");

        ResolvedPrompt result = resolver.resolvePrompt(
                PROMPT_KEY,
                fallbackPromptSupplier
        );

        assertThat(result.source()).isEqualTo(PromptSource.FALLBACK);
        assertThat(result.version()).isNull();
        assertThat(result.content()).isEqualTo("Fallback prompt");

        verify(fallbackPromptSupplier).get();
    }


    private AiPrompt createPrompt(
            String id,
            Long version,
            String content
    ) {
        return AiPrompt.builder()
                .id(id)
                .feature(PROMPT_KEY.feature())
                .type(PROMPT_KEY.type())
                .targetModel(PROMPT_KEY.targetModel())
                .version(version)
                .content(content)
                .active(true)
                .build();
    }
}