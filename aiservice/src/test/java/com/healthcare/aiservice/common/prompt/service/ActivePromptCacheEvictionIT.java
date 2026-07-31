package com.healthcare.aiservice.common.prompt.service;

import com.healthcare.aiservice.common.prompt.cache.CacheNames;
import com.healthcare.aiservice.common.prompt.cache.PromptCacheKey;
import com.healthcare.aiservice.common.prompt.model.AiPrompt;
import com.healthcare.aiservice.common.prompt.model.AiPromptKey;
import com.healthcare.aiservice.common.prompt.service.interfaces.AiPromptManagementService;
import com.healthcare.aiservice.config.AbstractMongoRedisIntegrationTest;
import com.healthcare.aiservice.config.constant.AiProviderModel;
import com.healthcare.aiservice.config.constant.FeatureName;
import com.healthcare.aiservice.config.constant.PromptType;
import com.healthcare.aiservice.repository.AiPromptRepository;
import org.junit.jupiter.api.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Active prompt cache eviction integration tests: ")
class ActivePromptCacheEvictionIT
        extends AbstractMongoRedisIntegrationTest {

    private static final String ACTIVE_PROMPT_ID =
            "active-prompt-id";

    private static final String INACTIVE_PROMPT_ID =
            "inactive-prompt-id";

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

    @Autowired
    private AiPromptRepository repository;

    @Autowired
    private CachedActivePromptService cachedActivePromptService;

    @Autowired
    private AiPromptManagementService promptManagementService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    @Qualifier("mongoTransactionManager")
    private PlatformTransactionManager transactionManager;

    @MockitoBean
    private ChatClient chatClient;

    @MockitoBean
    private DiscoveryClient discoveryClient;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        Cache cache = getActivePromptCache();
        cache.clear();

        repository.save(
                createPrompt(
                        ACTIVE_PROMPT_ID,
                        1L,
                        true,
                        "old active prompt"
                )
        );

        repository.save(
                createPrompt(
                        INACTIVE_PROMPT_ID,
                        2L,
                        false,
                        "new prompt"
                )
        );
    }

    @Test
    void activatePrompt_ShouldEvictCachedActivePrompt_AfterTransactionCommit() {

        AiPrompt cachedPrompt =
                cachedActivePromptService.findActivePrompt(PROMPT_KEY);

        assertThat(cachedPrompt)
                .isNotNull();

        assertThat(cachedPrompt.id())
                .isEqualTo(ACTIVE_PROMPT_ID);

        Cache cache = getActivePromptCache();
        String cacheKey = PromptCacheKey.of(PROMPT_KEY);

        assertThat(cache.get(cacheKey))
                .as("Active prompt must be present in cache before activation")
                .isNotNull();

        promptManagementService.activatePrompt(INACTIVE_PROMPT_ID);

        assertThat(cache.get(cacheKey))
                .as("Active prompt cache must be evicted after commit")
                .isNull();

        AiPrompt oldPrompt =
                repository.findById(ACTIVE_PROMPT_ID)
                        .orElseThrow();

        AiPrompt newPrompt =
                repository.findById(INACTIVE_PROMPT_ID)
                        .orElseThrow();

        assertThat(oldPrompt.active())
                .isFalse();

        assertThat(newPrompt.active())
                .isTrue();
    }

    @Test
    void activatePrompt_ShouldNotEvictCachedActivePrompt_WhenTransactionRollsBack() {
        AiPrompt cachedPrompt =
                cachedActivePromptService.findActivePrompt(PROMPT_KEY);

        assertThat(cachedPrompt)
                .isNotNull();

        assertThat(cachedPrompt.id())
                .isEqualTo(ACTIVE_PROMPT_ID);

        Cache cache = getActivePromptCache();
        String cacheKey = PromptCacheKey.of(PROMPT_KEY);

        assertThat(cache.get(cacheKey))
                .as("Old active prompt must be present in cache before transaction")
                .isNotNull();

        TransactionTemplate transactionTemplate =
                new TransactionTemplate(transactionManager);

        assertThatThrownBy(() ->
                transactionTemplate.executeWithoutResult(status -> {
                    promptManagementService.activatePrompt(
                            INACTIVE_PROMPT_ID
                    );

                    throw new TestRollbackException();
                })
        )
                .isInstanceOf(TestRollbackException.class);

        assertThat(cache.get(cacheKey))
                .as("Cache entry must remain after transaction rollback")
                .isNotNull();

        AiPrompt promptAfterRollback =
                cachedActivePromptService.findActivePrompt(PROMPT_KEY);

        assertThat(promptAfterRollback)
                .isNotNull();

        assertThat(promptAfterRollback.id())
                .isEqualTo(ACTIVE_PROMPT_ID);

        AiPrompt oldPromptAfterRollback =
                repository.findById(ACTIVE_PROMPT_ID)
                        .orElseThrow();

        AiPrompt newPromptAfterRollback =
                repository.findById(INACTIVE_PROMPT_ID)
                        .orElseThrow();

        assertThat(oldPromptAfterRollback.active())
                .isTrue();

        assertThat(newPromptAfterRollback.active())
                .isFalse();
    }

    private Cache getActivePromptCache() {
        Cache cache =
                cacheManager.getCache(CacheNames.ACTIVE_PROMPTS);

        assertThat(cache)
                .as("Active prompt cache must be configured")
                .isNotNull();

        return cache;
    }

    private AiPrompt createPrompt(
            String id,
            long version,
            boolean active,
            String content
    ) {
        return AiPrompt.builder()
                .id(id)
                .feature(FEATURE)
                .type(TYPE)
                .targetModel(TARGET_MODEL)
                .version(version)
                .content(content)
                .active(active)
                .createdByUserId("system")
                .createdByUsername("system")
                .build();
    }

    private static class TestRollbackException
            extends RuntimeException {
    }
}