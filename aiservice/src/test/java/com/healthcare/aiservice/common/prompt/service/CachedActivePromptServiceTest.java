package com.healthcare.aiservice.common.prompt.service;

import com.healthcare.aiservice.common.prompt.model.AiPrompt;
import com.healthcare.aiservice.common.prompt.model.AiPromptKey;
import com.healthcare.aiservice.config.constant.AiProviderModel;
import com.healthcare.aiservice.config.constant.PromptType;
import com.healthcare.aiservice.config.constant.FeatureName;
import com.healthcare.aiservice.exception.rest_exception.AiPromptStateInvalidException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Cached active prompt service tests: ")
class CachedActivePromptServiceTest {

    private static final AiPromptKey PROMPT_KEY = new AiPromptKey(
            FeatureName.MEDICAL_SUMMARY,
            PromptType.SYSTEM,
            AiProviderModel.LLAMA_3
    );

    @Mock
    private AiPromptRepository repository;

    @InjectMocks
    private CachedActivePromptService activePromptService;

    @Test
    void findActivePrompt_ShouldReturnPrompt_WhenSingleValidActivePromptExists() {
        AiPrompt activePrompt = createPrompt(
                "prompt-1",
                3L,
                "Database prompt"
        );

        when(repository.findAllByFeatureAndTypeAndTargetModelAndActiveTrue(
                PROMPT_KEY.feature(),
                PROMPT_KEY.type(),
                PROMPT_KEY.targetModel()
        )).thenReturn(List.of(activePrompt));

        AiPrompt result =
                activePromptService.findActivePrompt(PROMPT_KEY);

        assertThat(result).isSameAs(activePrompt);
        assertThat(result.id()).isEqualTo("prompt-1");
        assertThat(result.version()).isEqualTo(3L);
        assertThat(result.content()).isEqualTo("Database prompt");

        verify(repository)
                .findAllByFeatureAndTypeAndTargetModelAndActiveTrue(
                        PROMPT_KEY.feature(),
                        PROMPT_KEY.type(),
                        PROMPT_KEY.targetModel()
                );
    }

    @Test
    void findActivePrompt_ShouldReturnNull_WhenNoActivePromptExists() {
        when(repository.findAllByFeatureAndTypeAndTargetModelAndActiveTrue(
                PROMPT_KEY.feature(),
                PROMPT_KEY.type(),
                PROMPT_KEY.targetModel()
        )).thenReturn(List.of());

        AiPrompt result =
                activePromptService.findActivePrompt(PROMPT_KEY);

        assertThat(result).isNull();

        verify(repository)
                .findAllByFeatureAndTypeAndTargetModelAndActiveTrue(
                        PROMPT_KEY.feature(),
                        PROMPT_KEY.type(),
                        PROMPT_KEY.targetModel()
                );
    }

    @Test
    void findActivePrompt_ShouldReturnNull_WhenActivePromptContentIsBlank() {
        AiPrompt activePrompt = createPrompt(
                "prompt-1",
                3L,
                "   "
        );

        when(repository.findAllByFeatureAndTypeAndTargetModelAndActiveTrue(
                PROMPT_KEY.feature(),
                PROMPT_KEY.type(),
                PROMPT_KEY.targetModel()
        )).thenReturn(List.of(activePrompt));

        AiPrompt result =
                activePromptService.findActivePrompt(PROMPT_KEY);

        assertThat(result).isNull();

        verify(repository)
                .findAllByFeatureAndTypeAndTargetModelAndActiveTrue(
                        PROMPT_KEY.feature(),
                        PROMPT_KEY.type(),
                        PROMPT_KEY.targetModel()
                );
    }

    @Test
    void findActivePrompt_ShouldThrowException_WhenMultipleActivePromptsExist() {
        AiPrompt firstPrompt = createPrompt(
                "prompt-1",
                1L,
                "Prompt 1"
        );

        AiPrompt secondPrompt = createPrompt(
                "prompt-2",
                2L,
                "Prompt 2"
        );

        when(repository.findAllByFeatureAndTypeAndTargetModelAndActiveTrue(
                PROMPT_KEY.feature(),
                PROMPT_KEY.type(),
                PROMPT_KEY.targetModel()
        )).thenReturn(List.of(firstPrompt, secondPrompt));

        assertThatThrownBy(() ->
                activePromptService.findActivePrompt(PROMPT_KEY)
        )
                .isInstanceOf(AiPromptStateInvalidException.class);

        verify(repository)
                .findAllByFeatureAndTypeAndTargetModelAndActiveTrue(
                        PROMPT_KEY.feature(),
                        PROMPT_KEY.type(),
                        PROMPT_KEY.targetModel()
                );
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