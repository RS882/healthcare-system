package com.healthcare.aiservice.repository;

import com.healthcare.aiservice.common.prompt.model.AiPrompt;
import com.healthcare.aiservice.config.constant.AiProviderModel;
import com.healthcare.aiservice.config.constant.PromptType;
import com.healthcare.aiservice.config.AbstractMongoDbIntegrationTest;
import com.healthcare.aiservice.config.constant.FeatureName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers(disabledWithoutDocker = true)
@DataMongoTest(properties = {
        "spring.data.mongodb.auto-index-creation=true"
})
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("AI prompt repository integration tests: ")
class AiPromptRepositoryIntegrationTest
        extends AbstractMongoDbIntegrationTest {

    private static final FeatureName FEATURE =
            FeatureName.MEDICAL_SUMMARY;

    private static final PromptType TYPE =
            PromptType.SYSTEM;

    private static final AiProviderModel TARGET_MODEL =
            AiProviderModel.LLAMA_3;

    private static final Instant CREATED_AT =
            Instant.parse("2026-07-28T10:00:00Z");

    @Autowired
    private AiPromptRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void save_ShouldPersistPromptWithAllFields() {
        AiPrompt prompt = createPrompt(
                1L,
                false,
                "Prompt content version 1"
        );

        AiPrompt savedPrompt = repository.save(prompt);

        Optional<AiPrompt> foundPrompt =
                repository.findById(savedPrompt.id());

        assertThat(foundPrompt).isPresent();

        AiPrompt actual = foundPrompt.orElseThrow();

        assertThat(actual.id()).isNotBlank();
        assertThat(actual.feature()).isEqualTo(FEATURE);
        assertThat(actual.type()).isEqualTo(TYPE);
        assertThat(actual.targetModel()).isEqualTo(TARGET_MODEL);
        assertThat(actual.version()).isEqualTo(1L);
        assertThat(actual.content())
                .isEqualTo("Prompt content version 1");
        assertThat(actual.active()).isFalse();

        assertThat(actual.createdByUserId())
                .isEqualTo("system");

        assertThat(actual.createdByUsername())
                .isEqualTo("system");

        assertThat(actual.createdAt())
                .isEqualTo(CREATED_AT);

        assertThat(actual.updatedByUserId()).isNull();
        assertThat(actual.updatedByUsername()).isNull();
        assertThat(actual.updatedAt()).isNull();

        assertThat(actual.promptDescription())
                .isEqualTo("Medical summary system prompt");

        assertThat(actual.versionComment())
                .isEqualTo("Initial version");
    }

    @Test
    void findTopByFeatureAndTypeAndTargetModelOrderByVersionDesc_ShouldReturnLatestVersion() {
        repository.saveAll(List.of(
                createPrompt(
                        1L,
                        false,
                        "Version 1"
                ),
                createPrompt(
                        3L,
                        false,
                        "Version 3"
                ),
                createPrompt(
                        2L,
                        true,
                        "Version 2"
                )
        ));

        Optional<AiPrompt> result =
                repository
                        .findTopByFeatureAndTypeAndTargetModelOrderByVersionDesc(
                                FEATURE,
                                TYPE,
                                TARGET_MODEL
                        );

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().version()).isEqualTo(3L);
        assertThat(result.orElseThrow().content())
                .isEqualTo("Version 3");
    }

    @Test
    void findTopByFeatureAndTypeAndTargetModelOrderByVersionDesc_ShouldReturnEmpty_WhenNoPromptExists() {
        Optional<AiPrompt> result =
                repository
                        .findTopByFeatureAndTypeAndTargetModelOrderByVersionDesc(
                                FEATURE,
                                TYPE,
                                TARGET_MODEL
                        );

        assertThat(result).isEmpty();
    }

    @Test
    void findTopByFeatureAndTypeAndTargetModelOrderByVersionDesc_ShouldNotReturnPromptForDifferentKey() {
        repository.save(
                createPrompt(
                        FeatureName.MESSAGE_CLASSIFICATION,
                        TYPE,
                        TARGET_MODEL,
                        5L,
                        false,
                        "Other feature prompt"
                )
        );

        Optional<AiPrompt> result =
                repository
                        .findTopByFeatureAndTypeAndTargetModelOrderByVersionDesc(
                                FEATURE,
                                TYPE,
                                TARGET_MODEL
                        );

        assertThat(result).isEmpty();
    }

    @Test
    void findByFeatureAndTypeAndTargetModelOrderByVersionDesc_ShouldReturnVersionsInDescendingOrder() {
        repository.saveAll(List.of(
                createPrompt(
                        2L,
                        false,
                        "Version 2"
                ),
                createPrompt(
                        1L,
                        false,
                        "Version 1"
                ),
                createPrompt(
                        4L,
                        true,
                        "Version 4"
                ),
                createPrompt(
                        3L,
                        false,
                        "Version 3"
                )
        ));

        List<AiPrompt> result =
                repository
                        .findByFeatureAndTypeAndTargetModelOrderByVersionDesc(
                                FEATURE,
                                TYPE,
                                TARGET_MODEL
                        );

        assertThat(result).hasSize(4);

        assertThat(result)
                .extracting(AiPrompt::version)
                .containsExactly(
                        4L,
                        3L,
                        2L,
                        1L
                );

        assertThat(result)
                .extracting(AiPrompt::content)
                .containsExactly(
                        "Version 4",
                        "Version 3",
                        "Version 2",
                        "Version 1"
                );
    }

    @Test
    void findByFeatureAndTypeAndTargetModelOrderByVersionDesc_ShouldOnlyReturnMatchingPrompts() {
        repository.saveAll(List.of(
                createPrompt(
                        FEATURE,
                        TYPE,
                        TARGET_MODEL,
                        1L,
                        false,
                        "Matching prompt"
                ),
                createPrompt(
                        FeatureName.MESSAGE_CLASSIFICATION,
                        TYPE,
                        TARGET_MODEL,
                        1L,
                        false,
                        "Different feature"
                ),
                createPrompt(
                        FEATURE,
                        PromptType.USER,
                        TARGET_MODEL,
                        1L,
                        false,
                        "Different type"
                )
        ));

        List<AiPrompt> result =
                repository
                        .findByFeatureAndTypeAndTargetModelOrderByVersionDesc(
                                FEATURE,
                                TYPE,
                                TARGET_MODEL
                        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).content())
                .isEqualTo("Matching prompt");
    }

    @Test
    void findByFeatureAndTypeAndTargetModelOrderByVersionDesc_ShouldReturnEmptyList_WhenNoPromptExists() {
        List<AiPrompt> result =
                repository
                        .findByFeatureAndTypeAndTargetModelOrderByVersionDesc(
                                FEATURE,
                                TYPE,
                                TARGET_MODEL
                        );

        assertThat(result).isEmpty();
    }

    @Test
    void findByFeatureAndTypeAndTargetModelAndActiveTrue_ShouldReturnActivePrompt() {
        repository.saveAll(List.of(
                createPrompt(
                        1L,
                        false,
                        "Inactive prompt"
                ),
                createPrompt(
                        2L,
                        true,
                        "Active prompt"
                )
        ));

        Optional<AiPrompt> result =
                repository
                        .findByFeatureAndTypeAndTargetModelAndActiveTrue(
                                FEATURE,
                                TYPE,
                                TARGET_MODEL
                        );

        assertThat(result).isPresent();

        AiPrompt activePrompt = result.orElseThrow();

        assertThat(activePrompt.version()).isEqualTo(2L);
        assertThat(activePrompt.content())
                .isEqualTo("Active prompt");
        assertThat(activePrompt.active()).isTrue();
    }

    @Test
    void findByFeatureAndTypeAndTargetModelAndActiveTrue_ShouldReturnEmpty_WhenNoActivePromptExists() {
        repository.saveAll(List.of(
                createPrompt(
                        1L,
                        false,
                        "Version 1"
                ),
                createPrompt(
                        2L,
                        false,
                        "Version 2"
                )
        ));

        Optional<AiPrompt> result =
                repository
                        .findByFeatureAndTypeAndTargetModelAndActiveTrue(
                                FEATURE,
                                TYPE,
                                TARGET_MODEL
                        );

        assertThat(result).isEmpty();
    }

    @Test
    void findAllByFeatureAndTypeAndTargetModelAndActiveTrue_ShouldReturnAllActivePrompts() {
        repository.saveAll(List.of(
                createPrompt(
                        1L,
                        true,
                        "Active version 1"
                ),
                createPrompt(
                        2L,
                        false,
                        "Inactive version 2"
                ),
                createPrompt(
                        3L,
                        true,
                        "Active version 3"
                )
        ));

        List<AiPrompt> result =
                repository
                        .findAllByFeatureAndTypeAndTargetModelAndActiveTrue(
                                FEATURE,
                                TYPE,
                                TARGET_MODEL
                        );

        assertThat(result).hasSize(2);

        assertThat(result)
                .allMatch(AiPrompt::active);

        assertThat(result)
                .extracting(AiPrompt::version)
                .containsExactlyInAnyOrder(
                        1L,
                        3L
                );
    }

    @Test
    void findAllByFeatureAndTypeAndTargetModelAndActiveTrue_ShouldReturnEmptyList_WhenNoActivePromptExists() {
        repository.save(
                createPrompt(
                        1L,
                        false,
                        "Inactive prompt"
                )
        );

        List<AiPrompt> result =
                repository
                        .findAllByFeatureAndTypeAndTargetModelAndActiveTrue(
                                FEATURE,
                                TYPE,
                                TARGET_MODEL
                        );

        assertThat(result).isEmpty();
    }

    @Test
    void save_ShouldThrowDuplicateKeyException_WhenVersionAlreadyExistsForSamePromptKey() {
        AiPrompt firstPrompt = createPrompt(
                1L,
                false,
                "First prompt"
        );

        AiPrompt duplicateVersionPrompt = createPrompt(
                1L,
                true,
                "Duplicate prompt"
        );

        repository.save(firstPrompt);

        assertThatThrownBy(
                () -> repository.save(duplicateVersionPrompt)
        )
                .isInstanceOf(DuplicateKeyException.class);

        assertThat(repository.findAll()).hasSize(1);
    }

    @Test
    void save_ShouldAllowSameVersion_WhenFeatureIsDifferent() {
        AiPrompt firstPrompt = createPrompt(
                FEATURE,
                TYPE,
                TARGET_MODEL,
                1L,
                false,
                "Medical summary prompt"
        );

        AiPrompt secondPrompt = createPrompt(
                FeatureName.MESSAGE_CLASSIFICATION,
                TYPE,
                TARGET_MODEL,
                1L,
                false,
                "Classification prompt"
        );

        repository.save(firstPrompt);
        repository.save(secondPrompt);

        assertThat(repository.findAll()).hasSize(2);
    }

    @Test
    void save_ShouldAllowSameVersion_WhenPromptTypeIsDifferent() {
        AiPrompt systemPrompt = createPrompt(
                FEATURE,
                PromptType.SYSTEM,
                TARGET_MODEL,
                1L,
                false,
                "System prompt"
        );

        AiPrompt userPrompt = createPrompt(
                FEATURE,
                PromptType.USER,
                TARGET_MODEL,
                1L,
                false,
                "User prompt"
        );

        repository.save(systemPrompt);
        repository.save(userPrompt);

        assertThat(repository.findAll()).hasSize(2);
    }

    @Test
    void save_ShouldAllowMultipleActivePrompts_BecauseActiveIndexIsNotUnique() {
        AiPrompt firstActivePrompt = createPrompt(
                1L,
                true,
                "Active prompt version 1"
        );

        AiPrompt secondActivePrompt = createPrompt(
                2L,
                true,
                "Active prompt version 2"
        );

        repository.save(firstActivePrompt);
        repository.save(secondActivePrompt);

        List<AiPrompt> activePrompts =
                repository
                        .findAllByFeatureAndTypeAndTargetModelAndActiveTrue(
                                FEATURE,
                                TYPE,
                                TARGET_MODEL
                        );

        assertThat(activePrompts).hasSize(2);
    }

    @Test
    void save_ShouldPersistUpdatedActivationFields() {
        AiPrompt savedPrompt = repository.save(
                createPrompt(
                        1L,
                        false,
                        "Prompt content"
                )
        );

        AiPrompt activatedPrompt =
                savedPrompt.activate(
                        "admin-id",
                        "admin"
                );

        repository.save(activatedPrompt);

        AiPrompt actual = repository
                .findById(savedPrompt.id())
                .orElseThrow();

        assertThat(actual.active()).isTrue();
        assertThat(actual.updatedByUserId())
                .isEqualTo("admin-id");
        assertThat(actual.updatedByUsername())
                .isEqualTo("admin");
        assertThat(actual.updatedAt()).isNotNull();

        assertThat(actual.createdByUserId())
                .isEqualTo("system");
        assertThat(actual.createdAt())
                .isEqualTo(CREATED_AT);
    }

    private AiPrompt createPrompt(
            long version,
            boolean active,
            String content
    ) {
        return createPrompt(
                FEATURE,
                TYPE,
                TARGET_MODEL,
                version,
                active,
                content
        );
    }

    private AiPrompt createPrompt(
            FeatureName feature,
            PromptType type,
            AiProviderModel targetModel,
            long version,
            boolean active,
            String content
    ) {
        return AiPrompt.builder()
                .feature(feature)
                .type(type)
                .targetModel(targetModel)
                .version(version)
                .content(content)
                .active(active)
                .createdByUserId("system")
                .createdByUsername("system")
                .createdAt(CREATED_AT)
                .promptDescription(
                        "Medical summary system prompt"
                )
                .versionComment("Initial version")
                .build();
    }
}