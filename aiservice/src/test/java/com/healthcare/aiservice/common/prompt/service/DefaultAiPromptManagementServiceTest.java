package com.healthcare.aiservice.common.prompt.service;

import com.healthcare.aiservice.common.prompt.dto.AiPromptDetailsResponse;
import com.healthcare.aiservice.common.prompt.dto.AiPromptResponse;
import com.healthcare.aiservice.common.prompt.dto.CreateAiPromptRequest;
import com.healthcare.aiservice.common.prompt.model.AiPrompt;
import com.healthcare.aiservice.common.prompt.model.AiPromptKey;
import com.healthcare.aiservice.common.prompt.model.AiProviderModel;
import com.healthcare.aiservice.common.prompt.model.PromptType;
import com.healthcare.aiservice.common.prompt.normalizer.PromptTextNormalizer;
import com.healthcare.aiservice.config.constant.FeatureName;
import com.healthcare.aiservice.exception.AiActivePromptNotFoundException;
import com.healthcare.aiservice.exception.AiPromptNotFoundException;
import com.healthcare.aiservice.exception.AiPromptStateInvalidException;
import com.healthcare.aiservice.exception.AiPromptVersionConflictException;
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
import org.springframework.dao.DuplicateKeyException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Default AI prompt management service tests: ")
class DefaultAiPromptManagementServiceTest {

    private static final String PROMPT_ID = "prompt-id";
    private static final String SECOND_PROMPT_ID = "second-prompt-id";

    private static final String CONTENT = "Test prompt content";
    private static final String NORMALIZED_CONTENT = "Test prompt content";

    private static final String DESCRIPTION =
            "Medical summary system prompt";

    private static final String NORMALIZED_DESCRIPTION =
            "Medical summary system prompt";

    private static final String VERSION_COMMENT =
            "Initial prompt version";

    private static final String NORMALIZED_VERSION_COMMENT =
            "Initial prompt version";

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

    @Mock
    private PromptTextNormalizer normalizer;

    @InjectMocks
    private DefaultAiPromptManagementService service;

    @Test
    void createPrompt_ShouldCreateVersionOne_WhenNoPreviousPromptExists() {
        CreateAiPromptRequest request = createRequest();

        mockNormalization(request);

        when(repository
                .findTopByFeatureAndTypeAndTargetModelOrderByVersionDesc(
                        FEATURE,
                        TYPE,
                        TARGET_MODEL
                ))
                .thenReturn(Optional.empty());

        when(repository.save(any(AiPrompt.class)))
                .thenAnswer(invocation -> {
                    AiPrompt prompt = invocation.getArgument(0);

                    return copyWithId(prompt, PROMPT_ID);
                });

        AiPromptDetailsResponse result =
                service.createPrompt(request);

        ArgumentCaptor<AiPrompt> promptCaptor =
                ArgumentCaptor.forClass(AiPrompt.class);

        verify(repository).save(promptCaptor.capture());

        AiPrompt savedPrompt = promptCaptor.getValue();

        assertThat(savedPrompt.id()).isNull();
        assertThat(savedPrompt.feature()).isEqualTo(FEATURE);
        assertThat(savedPrompt.type()).isEqualTo(TYPE);
        assertThat(savedPrompt.targetModel()).isEqualTo(TARGET_MODEL);
        assertThat(savedPrompt.version()).isEqualTo(1L);
        assertThat(savedPrompt.content()).isEqualTo(NORMALIZED_CONTENT);
        assertThat(savedPrompt.active()).isFalse();

        assertThat(savedPrompt.createdByUserId())
                .isEqualTo("system");

        assertThat(savedPrompt.createdByUsername())
                .isEqualTo("system");

        assertThat(savedPrompt.createdAt()).isNotNull();

        assertThat(savedPrompt.promptDescription())
                .isEqualTo(NORMALIZED_DESCRIPTION);

        assertThat(savedPrompt.versionComment())
                .isEqualTo(NORMALIZED_VERSION_COMMENT);

        assertThat(savedPrompt.updatedByUserId()).isNull();
        assertThat(savedPrompt.updatedByUsername()).isNull();
        assertThat(savedPrompt.updatedAt()).isNull();

        assertThat(result.id()).isEqualTo(PROMPT_ID);
        assertThat(result.version()).isEqualTo(1L);
        assertThat(result.active()).isFalse();
        assertThat(result.content()).isEqualTo(NORMALIZED_CONTENT);

        verify(repository)
                .findTopByFeatureAndTypeAndTargetModelOrderByVersionDesc(
                        FEATURE,
                        TYPE,
                        TARGET_MODEL
                );

        verify(normalizer).normalizeContent(CONTENT);
        verify(normalizer).normalizeShortText(DESCRIPTION);
        verify(normalizer).normalizeShortText(VERSION_COMMENT);
    }

    @Test
    void createPrompt_ShouldCreateNextVersion_WhenPreviousPromptExists() {
        CreateAiPromptRequest request = createRequest();

        AiPrompt previousPrompt = createPrompt(
                PROMPT_ID,
                2L,
                false
        );

        mockNormalization(request);

        when(repository
                .findTopByFeatureAndTypeAndTargetModelOrderByVersionDesc(
                        FEATURE,
                        TYPE,
                        TARGET_MODEL
                ))
                .thenReturn(Optional.of(previousPrompt));

        when(repository.save(any(AiPrompt.class)))
                .thenAnswer(invocation -> {
                    AiPrompt prompt = invocation.getArgument(0);

                    return copyWithId(prompt, SECOND_PROMPT_ID);
                });

        AiPromptDetailsResponse result =
                service.createPrompt(request);

        ArgumentCaptor<AiPrompt> promptCaptor =
                ArgumentCaptor.forClass(AiPrompt.class);

        verify(repository).save(promptCaptor.capture());

        AiPrompt savedPrompt = promptCaptor.getValue();

        assertThat(savedPrompt.version()).isEqualTo(3L);
        assertThat(savedPrompt.active()).isFalse();
        assertThat(savedPrompt.feature()).isEqualTo(FEATURE);
        assertThat(savedPrompt.type()).isEqualTo(TYPE);
        assertThat(savedPrompt.targetModel()).isEqualTo(TARGET_MODEL);

        assertThat(result.id()).isEqualTo(SECOND_PROMPT_ID);
        assertThat(result.version()).isEqualTo(3L);
        assertThat(result.active()).isFalse();
    }

    @Test
    void createPrompt_ShouldCreateVersionOne_WhenPreviousVersionIsNotPositive() {
        CreateAiPromptRequest request = createRequest();

        AiPrompt invalidPreviousPrompt = createPrompt(
                PROMPT_ID,
                0L,
                false
        );

        mockNormalization(request);

        when(repository
                .findTopByFeatureAndTypeAndTargetModelOrderByVersionDesc(
                        FEATURE,
                        TYPE,
                        TARGET_MODEL
                ))
                .thenReturn(Optional.of(invalidPreviousPrompt));

        when(repository.save(any(AiPrompt.class)))
                .thenAnswer(invocation -> {
                    AiPrompt prompt = invocation.getArgument(0);

                    return copyWithId(prompt, SECOND_PROMPT_ID);
                });

        AiPromptDetailsResponse result =
                service.createPrompt(request);

        ArgumentCaptor<AiPrompt> promptCaptor =
                ArgumentCaptor.forClass(AiPrompt.class);

        verify(repository).save(promptCaptor.capture());

        assertThat(promptCaptor.getValue().version()).isEqualTo(1L);
        assertThat(result.version()).isEqualTo(1L);
    }

    @Test
    void createPrompt_ShouldThrowAiPromptVersionConflictException_WhenDuplicateVersionExists() {
        CreateAiPromptRequest request = createRequest();

        mockNormalization(request);

        when(repository
                .findTopByFeatureAndTypeAndTargetModelOrderByVersionDesc(
                        FEATURE,
                        TYPE,
                        TARGET_MODEL
                ))
                .thenReturn(Optional.empty());

        when(repository.save(any(AiPrompt.class)))
                .thenThrow(new DuplicateKeyException(
                        "Duplicate prompt version"
                ));

        assertThatThrownBy(() -> service.createPrompt(request))
                .isInstanceOf(AiPromptVersionConflictException.class);

        verify(repository).save(any(AiPrompt.class));
    }

    @Test
    void activatePrompt_ShouldActivateInactivePrompt_AndDeactivateOtherActivePrompts() {
        AiPrompt inactivePrompt = createPrompt(
                PROMPT_ID,
                2L,
                false
        );

        AiPrompt otherActivePrompt = createPrompt(
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
                .thenAnswer(invocation -> invocation.getArgument(0));

        AiPromptDetailsResponse result =
                service.activatePrompt(PROMPT_ID);

        ArgumentCaptor<AiPrompt> promptCaptor =
                ArgumentCaptor.forClass(AiPrompt.class);

        verify(repository, times(2))
                .save(promptCaptor.capture());

        List<AiPrompt> savedPrompts =
                promptCaptor.getAllValues();


        AiPrompt deactivatedPrompt = savedPrompts.get(0);


        AiPrompt activatedPrompt = savedPrompts.get(1);

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

        verify(publisher).publish(
                new AiPromptKey(
                        FEATURE,
                        TYPE,
                        TARGET_MODEL
                )
        );
    }
    @Test
    void activatePrompt_ShouldNotSavePromptAgain_WhenPromptIsAlreadyActive() {
        AiPrompt alreadyActivePrompt = createPrompt(
                PROMPT_ID,
                2L,
                true
        );

        AiPrompt otherActivePrompt = createPrompt(
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
                .thenAnswer(invocation -> invocation.getArgument(0));

        AiPromptDetailsResponse result =
                service.activatePrompt(PROMPT_ID);

        ArgumentCaptor<AiPrompt> promptCaptor =
                ArgumentCaptor.forClass(AiPrompt.class);

        verify(repository).save(promptCaptor.capture());

        AiPrompt savedPrompt = promptCaptor.getValue();

        assertThat(savedPrompt.id()).isEqualTo(SECOND_PROMPT_ID);
        assertThat(savedPrompt.active()).isFalse();

        assertThat(result.id()).isEqualTo(PROMPT_ID);
        assertThat(result.active()).isTrue();
    }

    @Test
    void activatePrompt_ShouldNotSaveAnything_WhenPromptIsAlreadyActiveAndNoOtherActivePromptsExist() {
        AiPrompt alreadyActivePrompt = createPrompt(
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

        assertThat(result.id()).isEqualTo(PROMPT_ID);
        assertThat(result.active()).isTrue();

        verify(repository, never()).save(any(AiPrompt.class));
    }

    @Test
    void getPrompt_ShouldReturnPromptDetails_WhenPromptExists() {
        AiPrompt prompt = createPrompt(
                PROMPT_ID,
                1L,
                false
        );

        when(repository.findById(PROMPT_ID))
                .thenReturn(Optional.of(prompt));

        AiPromptDetailsResponse result =
                service.getPrompt(PROMPT_ID);

        assertThat(result.id()).isEqualTo(PROMPT_ID);
        assertThat(result.feature()).isEqualTo(FEATURE);
        assertThat(result.type()).isEqualTo(TYPE);
        assertThat(result.targetModel()).isEqualTo(TARGET_MODEL);
        assertThat(result.version()).isEqualTo(1L);
        assertThat(result.active()).isFalse();
        assertThat(result.content()).isEqualTo(NORMALIZED_CONTENT);

        verify(repository).findById(PROMPT_ID);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void getPrompt_ShouldThrowAiPromptNotFoundException_WhenPromptDoesNotExist() {
        when(repository.findById(PROMPT_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPrompt(PROMPT_ID))
                .isInstanceOf(AiPromptNotFoundException.class);

        verify(repository).findById(PROMPT_ID);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void activatePrompt_ShouldThrowAiPromptNotFoundException_WhenPromptDoesNotExist() {
        when(repository.findById(PROMPT_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.activatePrompt(PROMPT_ID))
                .isInstanceOf(AiPromptNotFoundException.class);

        verify(repository).findById(PROMPT_ID);
        verify(repository, never()).save(any(AiPrompt.class));
    }

    @Test
    void getActivePrompt_ShouldReturnActivePrompt_WhenExactlyOneExists() {
        AiPrompt activePrompt = createPrompt(
                PROMPT_ID,
                2L,
                true
        );

        when(repository
                .findAllByFeatureAndTypeAndTargetModelAndActiveTrue(
                        FEATURE,
                        TYPE,
                        TARGET_MODEL
                ))
                .thenReturn(List.of(activePrompt));

        AiPromptDetailsResponse result =
                service.getActivePrompt(PROMPT_KEY);

        assertThat(result.id()).isEqualTo(PROMPT_ID);
        assertThat(result.version()).isEqualTo(2L);
        assertThat(result.active()).isTrue();

        verify(repository)
                .findAllByFeatureAndTypeAndTargetModelAndActiveTrue(
                        FEATURE,
                        TYPE,
                        TARGET_MODEL
                );
    }

    @Test
    void getActivePrompt_ShouldThrowAiActivePromptNotFoundException_WhenNoActivePromptExists() {
        when(repository
                .findAllByFeatureAndTypeAndTargetModelAndActiveTrue(
                        FEATURE,
                        TYPE,
                        TARGET_MODEL
                ))
                .thenReturn(List.of());

        assertThatThrownBy(() -> service.getActivePrompt(PROMPT_KEY))
                .isInstanceOf(AiActivePromptNotFoundException.class);

        verify(repository)
                .findAllByFeatureAndTypeAndTargetModelAndActiveTrue(
                        FEATURE,
                        TYPE,
                        TARGET_MODEL
                );
    }

    @Test
    void getActivePrompt_ShouldThrowAiPromptStateInvalidException_WhenMultipleActivePromptsExist() {
        AiPrompt firstPrompt = createPrompt(
                PROMPT_ID,
                1L,
                true
        );

        AiPrompt secondPrompt = createPrompt(
                SECOND_PROMPT_ID,
                2L,
                true
        );

        when(repository
                .findAllByFeatureAndTypeAndTargetModelAndActiveTrue(
                        FEATURE,
                        TYPE,
                        TARGET_MODEL
                ))
                .thenReturn(List.of(
                        firstPrompt,
                        secondPrompt
                ));

        assertThatThrownBy(() -> service.getActivePrompt(PROMPT_KEY))
                .isInstanceOf(AiPromptStateInvalidException.class);

        verify(repository)
                .findAllByFeatureAndTypeAndTargetModelAndActiveTrue(
                        FEATURE,
                        TYPE,
                        TARGET_MODEL
                );
    }

    @Test
    void getPromptVersions_ShouldReturnPromptVersions_InRepositoryOrder() {
        AiPrompt versionThree = createPrompt(
                "prompt-v3",
                3L,
                false
        );

        AiPrompt versionTwo = createPrompt(
                "prompt-v2",
                2L,
                true
        );

        AiPrompt versionOne = createPrompt(
                "prompt-v1",
                1L,
                false
        );

        when(repository
                .findByFeatureAndTypeAndTargetModelOrderByVersionDesc(
                        FEATURE,
                        TYPE,
                        TARGET_MODEL
                ))
                .thenReturn(List.of(
                        versionThree,
                        versionTwo,
                        versionOne
                ));

        List<AiPromptResponse> result =
                service.getPromptVersions(PROMPT_KEY);

        assertThat(result).hasSize(3);

        assertThat(result)
                .extracting(AiPromptResponse::version)
                .containsExactly(3L, 2L, 1L);

        assertThat(result)
                .extracting(AiPromptResponse::id)
                .containsExactly(
                        "prompt-v3",
                        "prompt-v2",
                        "prompt-v1"
                );

        verify(repository)
                .findByFeatureAndTypeAndTargetModelOrderByVersionDesc(
                        FEATURE,
                        TYPE,
                        TARGET_MODEL
                );
    }

    private CreateAiPromptRequest createRequest() {
        return new CreateAiPromptRequest(
                FEATURE,
                TYPE,
                TARGET_MODEL,
                CONTENT,
                DESCRIPTION,
                VERSION_COMMENT
        );
    }

    private void mockNormalization(CreateAiPromptRequest request) {
        when(normalizer.normalizeContent(request.content()))
                .thenReturn(NORMALIZED_CONTENT);

        when(normalizer.normalizeShortText(
                request.promptDescription()
        )).thenReturn(NORMALIZED_DESCRIPTION);

        when(normalizer.normalizeShortText(
                request.versionComment()
        )).thenReturn(NORMALIZED_VERSION_COMMENT);
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
                .content(NORMALIZED_CONTENT)
                .active(active)
                .createdByUserId("system")
                .createdByUsername("system")
                .createdAt(Instant.parse("2026-07-28T10:00:00Z"))
                .promptDescription(NORMALIZED_DESCRIPTION)
                .versionComment(NORMALIZED_VERSION_COMMENT)
                .build();
    }

    private AiPrompt copyWithId(
            AiPrompt prompt,
            String id
    ) {
        return AiPrompt.builder()
                .id(id)
                .feature(prompt.feature())
                .type(prompt.type())
                .targetModel(prompt.targetModel())
                .version(prompt.version())
                .content(prompt.content())
                .active(prompt.active())
                .createdByUserId(prompt.createdByUserId())
                .createdByUsername(prompt.createdByUsername())
                .updatedByUserId(prompt.updatedByUserId())
                .updatedByUsername(prompt.updatedByUsername())
                .createdAt(prompt.createdAt())
                .updatedAt(prompt.updatedAt())
                .promptDescription(prompt.promptDescription())
                .versionComment(prompt.versionComment())
                .build();
    }
}