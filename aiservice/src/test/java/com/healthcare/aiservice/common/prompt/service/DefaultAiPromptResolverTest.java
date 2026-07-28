package com.healthcare.aiservice.common.prompt.service;


import com.healthcare.aiservice.common.prompt.model.AiPrompt;
import com.healthcare.aiservice.common.prompt.model.AiPromptKey;
import com.healthcare.aiservice.common.prompt.model.AiProviderModel;
import com.healthcare.aiservice.common.prompt.model.PromptSource;
import com.healthcare.aiservice.common.prompt.model.PromptType;
import com.healthcare.aiservice.common.prompt.model.ResolvedPrompt;
import com.healthcare.aiservice.config.constant.FeatureName;
import com.healthcare.aiservice.exception.AiPromptStateInvalidException;
import com.healthcare.aiservice.repository.AiPromptRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private AiPromptRepository repository;

    @Mock
    private Supplier<String> fallbackPromptSupplier;

    @InjectMocks
    private DefaultAiPromptResolver resolver;

    @Test
    void resolvePrompt_ShouldReturnDatabasePrompt_WhenSingleActivePromptExists() {
        AiPrompt activePrompt = AiPrompt.builder()
                .id("prompt-id")
                .feature(PROMPT_KEY.feature())
                .type(PROMPT_KEY.type())
                .targetModel(PROMPT_KEY.targetModel())
                .version(3L)
                .content("Database prompt")
                .active(true)
                .build();

        when(repository.findAllByFeatureAndTypeAndTargetModelAndActiveTrue(
                PROMPT_KEY.feature(),
                PROMPT_KEY.type(),
                PROMPT_KEY.targetModel()
        )).thenReturn(List.of(activePrompt));

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
        when(repository.findAllByFeatureAndTypeAndTargetModelAndActiveTrue(
                PROMPT_KEY.feature(),
                PROMPT_KEY.type(),
                PROMPT_KEY.targetModel()
        )).thenReturn(List.of());

        when(fallbackPromptSupplier.get()).thenReturn("Fallback prompt");

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
        AiPrompt activePrompt = AiPrompt.builder()
                .id("prompt-id")
                .feature(PROMPT_KEY.feature())
                .type(PROMPT_KEY.type())
                .targetModel(PROMPT_KEY.targetModel())
                .version(3L)
                .content("   ")
                .active(true)
                .build();

        when(repository.findAllByFeatureAndTypeAndTargetModelAndActiveTrue(
                PROMPT_KEY.feature(),
                PROMPT_KEY.type(),
                PROMPT_KEY.targetModel()
        )).thenReturn(List.of(activePrompt));

        when(fallbackPromptSupplier.get()).thenReturn("Fallback prompt");

        ResolvedPrompt result = resolver.resolvePrompt(
                PROMPT_KEY,
                fallbackPromptSupplier
        );

        assertThat(result.source()).isEqualTo(PromptSource.FALLBACK);
        assertThat(result.version()).isNull();
        assertThat(result.content()).isEqualTo("Fallback prompt");

        verify(fallbackPromptSupplier).get();
    }

    @Test
    void resolvePrompt_ShouldThrowException_WhenMultipleActivePromptsExist() {
        AiPrompt firstPrompt = AiPrompt.builder()
                .id("prompt-1")
                .feature(PROMPT_KEY.feature())
                .type(PROMPT_KEY.type())
                .targetModel(PROMPT_KEY.targetModel())
                .version(1L)
                .content("Prompt 1")
                .active(true)
                .build();

        AiPrompt secondPrompt = AiPrompt.builder()
                .id("prompt-2")
                .feature(PROMPT_KEY.feature())
                .type(PROMPT_KEY.type())
                .targetModel(PROMPT_KEY.targetModel())
                .version(2L)
                .content("Prompt 2")
                .active(true)
                .build();

        when(repository.findAllByFeatureAndTypeAndTargetModelAndActiveTrue(
                PROMPT_KEY.feature(),
                PROMPT_KEY.type(),
                PROMPT_KEY.targetModel()
        )).thenReturn(List.of(firstPrompt, secondPrompt));

        assertThatThrownBy(() -> resolver.resolvePrompt(
                PROMPT_KEY,
                fallbackPromptSupplier
        ))
                .isInstanceOf(AiPromptStateInvalidException.class);

        verify(fallbackPromptSupplier, never()).get();
    }
}