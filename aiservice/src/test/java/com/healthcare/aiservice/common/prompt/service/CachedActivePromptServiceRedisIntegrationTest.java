package com.healthcare.aiservice.common.prompt.service;

import com.healthcare.aiservice.common.prompt.cache.CacheNames;
import com.healthcare.aiservice.common.prompt.cache.PromptCacheKey;
import com.healthcare.aiservice.common.prompt.model.AiPrompt;
import com.healthcare.aiservice.common.prompt.model.AiPromptKey;
import com.healthcare.aiservice.config.constant.AiProviderModel;
import com.healthcare.aiservice.config.constant.PromptType;
import com.healthcare.aiservice.config.AbstractRedisIntegrationTest;
import com.healthcare.aiservice.config.constant.FeatureName;
import com.healthcare.aiservice.repository.AiPromptRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Cached active prompt Redis integration tests: ")
class CachedActivePromptServiceRedisIntegrationTest
        extends AbstractRedisIntegrationTest {

    private static final AiPromptKey PROMPT_KEY = new AiPromptKey(
            FeatureName.MEDICAL_SUMMARY,
            PromptType.SYSTEM,
            AiProviderModel.LLAMA_3
    );

    @Autowired
    private CachedActivePromptService service;

    @MockitoBean
    private AiPromptRepository repository;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        Cache cache = cacheManager.getCache(CacheNames.ACTIVE_PROMPTS);

        assertThat(cache).isNotNull();

        cache.clear();
        reset(repository);
    }

    @Test
    void findActivePrompt_ShouldStoreAndReadPromptFromRedis() {

        AiPrompt prompt = createPrompt();

        when(repository.findAllByFeatureAndTypeAndTargetModelAndActiveTrue(
                PROMPT_KEY.feature(),
                PROMPT_KEY.type(),
                PROMPT_KEY.targetModel()
        )).thenReturn(List.of(prompt));

        AiPrompt firstResult = service.findActivePrompt(PROMPT_KEY);
        AiPrompt secondResult = service.findActivePrompt(PROMPT_KEY);

        assertThat(cacheManager)
                .isInstanceOf(RedisCacheManager.class);

        assertThat(firstResult)
                .usingRecursiveComparison()
                .isEqualTo(prompt);

        assertThat(secondResult)
                .usingRecursiveComparison()
                .isEqualTo(prompt);

        verify(repository, times(1))
                .findAllByFeatureAndTypeAndTargetModelAndActiveTrue(
                        PROMPT_KEY.feature(),
                        PROMPT_KEY.type(),
                        PROMPT_KEY.targetModel()
                );

        assertThat(redisTemplate.hasKey(redisKey()))
                .isTrue();
    }

    @Test
    void findActivePrompt_ShouldNotStoreNullResultInRedis() {

        when(repository.findAllByFeatureAndTypeAndTargetModelAndActiveTrue(
                PROMPT_KEY.feature(),
                PROMPT_KEY.type(),
                PROMPT_KEY.targetModel()
        )).thenReturn(List.of());

        AiPrompt firstResult = service.findActivePrompt(PROMPT_KEY);
        AiPrompt secondResult = service.findActivePrompt(PROMPT_KEY);

        assertThat(firstResult).isNull();
        assertThat(secondResult).isNull();

        verify(repository, times(2))
                .findAllByFeatureAndTypeAndTargetModelAndActiveTrue(
                        PROMPT_KEY.feature(),
                        PROMPT_KEY.type(),
                        PROMPT_KEY.targetModel()
                );

        assertThat(redisTemplate.hasKey(redisKey()))
                .isFalse();
    }

    @Test
    void findActivePrompt_ShouldSetTtlForCachedPrompt() {

        when(repository.findAllByFeatureAndTypeAndTargetModelAndActiveTrue(
                PROMPT_KEY.feature(),
                PROMPT_KEY.type(),
                PROMPT_KEY.targetModel()
        )).thenReturn(List.of(createPrompt()));

        service.findActivePrompt(PROMPT_KEY);

        Long ttlSeconds = redisTemplate.getExpire(
                redisKey(),
                TimeUnit.SECONDS
        );

        assertThat(ttlSeconds)
                .isNotNull()
                .isPositive()
                .isLessThanOrEqualTo(3600);
    }

    private String redisKey() {
        return CacheNames.ACTIVE_PROMPTS
                + "::"
                + PromptCacheKey.of(PROMPT_KEY);
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
}