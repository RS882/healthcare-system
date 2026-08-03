package com.healthcare.aiservice.common.prompt.service;

import com.healthcare.aiservice.common.prompt.dto.AiPromptDetailsResponse;
import com.healthcare.aiservice.common.prompt.model.AiPrompt;
import com.healthcare.aiservice.common.prompt.model.AiPromptKey;
import com.healthcare.aiservice.config.constant.AiProviderModel;
import com.healthcare.aiservice.config.constant.FeatureName;
import com.healthcare.aiservice.config.constant.PromptType;
import com.healthcare.aiservice.exception.AiPromptNotFoundException;
import com.healthcare.aiservice.repository.AiPromptRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Prompt activation transactional service tests: ")
class DefaultPromptActivationTransactionalServiceTest {

    private static final String PROMPT_ID =
            "prompt-id";

    private static final String SECOND_PROMPT_ID =
            "second-prompt-id";

    private static final FeatureName FEATURE =
            FeatureName.MEDICAL_SUMMARY;

    private static final PromptType TYPE =
            PromptType.SYSTEM;

    private static final AiProviderModel TARGET_MODEL =
            AiProviderModel.LLAMA_3;

    private static final AiPromptKey PROMPT_KEY =
            new AiPromptKey(
                    FEATURE,
                    TYPE,
                    TARGET_MODEL
            );

    @Mock
    private AiPromptRepository repository;

    @Mock
    private ActivePromptChangedEventPublisher publisher;

    @InjectMocks
    private DefaultPromptActivationTransactionalService service;

    @Test
    void activatePrompt_ShouldActivateInactivePrompt_AndDeactivateOtherActivePrompts() {
        AiPrompt inactivePrompt =
                createPrompt(
                        PROMPT_ID,
                        2L,
                        false
                );

        AiPrompt otherActivePrompt =
                createPrompt(
                        SECOND_PROMPT_ID,
                        1L,
                        true
                );

        when(repository.findById(PROMPT_ID))
                .thenReturn(Optional.of(inactivePrompt));

        when(repository
                .findAllByFeatureAndTypeAndTargetModelAndActiveTrue(
                        FEATURE,
                        TYPE,
                        TARGET_MODEL
                ))
                .thenReturn(List.of(otherActivePrompt));

        when(repository.save(any(AiPrompt.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        AiPromptDetailsResponse result =
                service.activatePrompt(PROMPT_ID);

        ArgumentCaptor<AiPrompt> promptCaptor =
                ArgumentCaptor.forClass(AiPrompt.class);

        verify(repository, times(2))
                .save(promptCaptor.capture());

        List<AiPrompt> savedPrompts =
                promptCaptor.getAllValues();

        AiPrompt deactivatedPrompt =
                savedPrompts.get(0);

        AiPrompt activatedPrompt =
                savedPrompts.get(1);

        assertThat(deactivatedPrompt.id())
                .isEqualTo(SECOND_PROMPT_ID);

        assertThat(deactivatedPrompt.active())
                .isFalse();

        assertThat(deactivatedPrompt.updatedByUserId())
                .isEqualTo("system");

        assertThat(deactivatedPrompt.updatedByUsername())
                .isEqualTo("system");

        assertThat(deactivatedPrompt.updatedAt())
                .isNotNull();

        assertThat(activatedPrompt.id())
                .isEqualTo(PROMPT_ID);

        assertThat(activatedPrompt.active())
                .isTrue();

        assertThat(activatedPrompt.updatedByUserId())
                .isEqualTo("system");

        assertThat(activatedPrompt.updatedByUsername())
                .isEqualTo("system");

        assertThat(activatedPrompt.updatedAt())
                .isNotNull();

        assertThat(result.id())
                .isEqualTo(PROMPT_ID);

        assertThat(result.active())
                .isTrue();

        verify(publisher)
                .publish(PROMPT_KEY);
    }

    @Test
    void activatePrompt_ShouldNotSavePromptAgain_WhenPromptIsAlreadyActive() {
        AiPrompt alreadyActivePrompt =
                createPrompt(
                        PROMPT_ID,
                        2L,
                        true
                );

        AiPrompt otherActivePrompt =
                createPrompt(
                        SECOND_PROMPT_ID,
                        1L,
                        true
                );

        when(repository.findById(PROMPT_ID))
                .thenReturn(Optional.of(alreadyActivePrompt));

        when(repository
                .findAllByFeatureAndTypeAndTargetModelAndActiveTrue(
                        FEATURE,
                        TYPE,
                        TARGET_MODEL
                ))
                .thenReturn(List.of(
                        alreadyActivePrompt,
                        otherActivePrompt
                ));

        when(repository.save(any(AiPrompt.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        AiPromptDetailsResponse result =
                service.activatePrompt(PROMPT_ID);

        ArgumentCaptor<AiPrompt> promptCaptor =
                ArgumentCaptor.forClass(AiPrompt.class);

        verify(repository)
                .save(promptCaptor.capture());

        AiPrompt savedPrompt =
                promptCaptor.getValue();

        assertThat(savedPrompt.id())
                .isEqualTo(SECOND_PROMPT_ID);

        assertThat(savedPrompt.active())
                .isFalse();

        assertThat(result.id())
                .isEqualTo(PROMPT_ID);

        assertThat(result.active())
                .isTrue();

        verify(publisher)
                .publish(PROMPT_KEY);
    }

    @Test
    void activatePrompt_ShouldNotSaveAnything_WhenPromptIsAlreadyActiveAndNoOtherActivePromptsExist() {
        AiPrompt alreadyActivePrompt =
                createPrompt(
                        PROMPT_ID,
                        1L,
                        true
                );

        when(repository.findById(PROMPT_ID))
                .thenReturn(Optional.of(alreadyActivePrompt));

        when(repository
                .findAllByFeatureAndTypeAndTargetModelAndActiveTrue(
                        FEATURE,
                        TYPE,
                        TARGET_MODEL
                ))
                .thenReturn(List.of(alreadyActivePrompt));

        AiPromptDetailsResponse result =
                service.activatePrompt(PROMPT_ID);

        assertThat(result.id())
                .isEqualTo(PROMPT_ID);

        assertThat(result.active())
                .isTrue();

        verify(repository, never())
                .save(any(AiPrompt.class));

        verify(publisher)
                .publish(PROMPT_KEY);
    }

    @Test
    void activatePrompt_ShouldThrowAiPromptNotFoundException_WhenPromptDoesNotExist() {
        when(repository.findById(PROMPT_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> service.activatePrompt(PROMPT_ID)
        )
                .isInstanceOf(
                        AiPromptNotFoundException.class
                );

        verify(repository)
                .findById(PROMPT_ID);

        verify(repository, never())
                .save(any(AiPrompt.class));

        verify(publisher, never())
                .publish(any(AiPromptKey.class));
    }

    private AiPrompt createPrompt(
            String id,
            long version,
            boolean active
    ) {
        return AiPrompt.builder()
                .id(id)
                .feature(FEATURE)
                .type(TYPE)
                .targetModel(TARGET_MODEL)
                .version(version)
                .content("Test prompt content")
                .active(active)
                .createdByUserId("system")
                .createdByUsername("system")
                .createdAt(
                        Instant.parse(
                                "2026-07-28T10:00:00Z"
                        )
                )
                .promptDescription(
                        "Medical summary system prompt"
                )
                .versionComment(
                        "Initial prompt version"
                )
                .build();
    }
}
