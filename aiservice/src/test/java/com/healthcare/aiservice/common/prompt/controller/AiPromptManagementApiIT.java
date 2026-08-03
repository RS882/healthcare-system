package com.healthcare.aiservice.common.prompt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.aiservice.common.prompt.cache.CacheNames;
import com.healthcare.aiservice.common.prompt.cache.PromptCacheKey;
import com.healthcare.aiservice.common.prompt.dto.CreateAiPromptRequest;
import com.healthcare.aiservice.common.prompt.model.AiPrompt;
import com.healthcare.aiservice.common.prompt.model.AiPromptKey;
import com.healthcare.aiservice.common.prompt.service.CachedActivePromptService;
import com.healthcare.aiservice.config.AbstractMongoRedisIntegrationTest;
import com.healthcare.aiservice.config.constant.AiProviderModel;
import com.healthcare.aiservice.config.constant.FeatureName;
import com.healthcare.aiservice.config.constant.PromptType;
import com.healthcare.aiservice.exception.ErrorCode;
import com.healthcare.aiservice.repository.AiPromptRepository;
import org.junit.jupiter.api.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexField;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

import static com.healthcare.aiservice.common.prompt.controller.API.AiPromptApiPaths.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("AI prompt management API integration tests: ")
class AiPromptManagementApiIT
        extends AbstractMongoRedisIntegrationTest {

    private static final FeatureName FEATURE =
            FeatureName.MEDICAL_SUMMARY;

    private static final PromptType TYPE =
            PromptType.SYSTEM;

    private static final AiProviderModel TARGET_MODEL =
            AiProviderModel.LLAMA_3;

    private static final String PROMPT_CONTENT = """
            You are a medical summary assistant.
            
            Use only information explicitly stated in the medical note.
            Return exactly one valid JSON object.
            """;

    private static final String PROMPT_DESCRIPTION =
            "Medical Summary System Prompt";

    private static final String VERSION_COMMENT =
            "Initial production version";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AiPromptRepository repository;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private CachedActivePromptService cachedActivePromptService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @MockitoBean
    private ChatClient chatClient;

    @MockitoBean
    private DiscoveryClient discoveryClient;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        Cache activePromptCache =
                cacheManager.getCache(CacheNames.ACTIVE_PROMPTS);

        assertThat(activePromptCache)
                .as("Active prompt cache must be configured")
                .isNotNull();

        activePromptCache.clear();
    }

    @Test
    void createPrompt_ShouldCreateFirstInactiveVersionAndPersistItInMongo()
            throws Exception {

        CreateAiPromptRequest request =
                createPromptRequest();

        mockMvc.perform(post(PROMPTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.feature")
                        .value(FEATURE.name()))
                .andExpect(jsonPath("$.type")
                        .value(TYPE.name()))
                .andExpect(jsonPath("$.targetModel")
                        .value(TARGET_MODEL.name()))
                .andExpect(jsonPath("$.version")
                        .value(1))
                .andExpect(jsonPath("$.content")
                        .value(PROMPT_CONTENT.strip()))
                .andExpect(jsonPath("$.active")
                        .value(false))
                .andExpect(jsonPath("$.description")
                        .value(PROMPT_DESCRIPTION))
                .andExpect(jsonPath("$.versionComment")
                        .value(VERSION_COMMENT))
                .andExpect(jsonPath("$.createdByUserId")
                        .value("system"))
                .andExpect(jsonPath("$.createdByUsername")
                        .value("system"))
                .andExpect(jsonPath("$.createdAt")
                        .isNotEmpty())
                .andExpect(jsonPath("$.updatedByUserId")
                        .doesNotExist())
                .andExpect(jsonPath("$.updatedByUsername")
                        .doesNotExist())
                .andExpect(jsonPath("$.updatedAt")
                        .doesNotExist());

        List<AiPrompt> savedPrompts =
                repository.findAll();

        assertThat(savedPrompts)
                .hasSize(1);

        AiPrompt savedPrompt =
                savedPrompts.get(0);

        assertThat(savedPrompt.id())
                .isNotBlank();

        assertThat(savedPrompt.feature())
                .isEqualTo(FEATURE);

        assertThat(savedPrompt.type())
                .isEqualTo(TYPE);

        assertThat(savedPrompt.targetModel())
                .isEqualTo(TARGET_MODEL);

        assertThat(savedPrompt.version())
                .isEqualTo(1L);

        assertThat(savedPrompt.content())
                .isEqualTo(PROMPT_CONTENT.strip());

        assertThat(savedPrompt.active())
                .isFalse();

        assertThat(savedPrompt.promptDescription())
                .isEqualTo(PROMPT_DESCRIPTION);

        assertThat(savedPrompt.versionComment())
                .isEqualTo(VERSION_COMMENT);

        assertThat(savedPrompt.createdByUserId())
                .isEqualTo("system");

        assertThat(savedPrompt.createdByUsername())
                .isEqualTo("system");

        assertThat(savedPrompt.createdAt())
                .isNotNull();

        assertThat(savedPrompt.updatedByUserId())
                .isNull();

        assertThat(savedPrompt.updatedByUsername())
                .isNull();

        assertThat(savedPrompt.updatedAt())
                .isNull();
    }

    @Test
    void createPrompt_ShouldCreateNextVersionForSamePromptKey()
            throws Exception {

        CreateAiPromptRequest firstRequest =
                new CreateAiPromptRequest(
                        FEATURE,
                        TYPE,
                        TARGET_MODEL,
                        """
                                You are a medical summary assistant.
                                
                                Use only explicitly stated information.
                                Return valid JSON only.
                                """,
                        "Medical Summary System Prompt",
                        "Initial version"
                );

        CreateAiPromptRequest secondRequest =
                new CreateAiPromptRequest(
                        FEATURE,
                        TYPE,
                        TARGET_MODEL,
                        """
                                You are a medical summary assistant.
                                
                                Use only explicitly stated information.
                                Return valid JSON only.
                                Include medication dosage when it is explicitly provided.
                                """,
                        "Medical Summary System Prompt",
                        "Added medication dosage rule"
                );

        mockMvc.perform(post(PROMPTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(post(PROMPTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.version").value(2))
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.content")
                        .value(secondRequest.content().strip()))
                .andExpect(jsonPath("$.versionComment")
                        .value(secondRequest.versionComment()));

        List<AiPrompt> savedPrompts =
                repository.findByFeatureAndTypeAndTargetModelOrderByVersionDesc(
                        FEATURE,
                        TYPE,
                        TARGET_MODEL
                );

        assertThat(savedPrompts)
                .hasSize(2);

        AiPrompt versionTwo = savedPrompts.get(0);
        AiPrompt versionOne = savedPrompts.get(1);

        assertThat(versionTwo.version())
                .isEqualTo(2L);

        assertThat(versionTwo.active())
                .isFalse();

        assertThat(versionTwo.content())
                .isEqualTo(secondRequest.content().strip());

        assertThat(versionTwo.versionComment())
                .isEqualTo("Added medication dosage rule");

        assertThat(versionOne.version())
                .isEqualTo(1L);

        assertThat(versionOne.active())
                .isFalse();

        assertThat(versionOne.content())
                .isEqualTo(firstRequest.content().strip());

        Cache cache =
                cacheManager.getCache(CacheNames.ACTIVE_PROMPTS);

        assertThat(cache)
                .isNotNull();

        assertThat(cache.get(PromptCacheKey.of(
                new AiPromptKey(
                        FEATURE,
                        TYPE,
                        TARGET_MODEL
                )
        )))
                .as("Creating inactive versions must not populate or evict active prompt cache")
                .isNull();
    }

    @Test
    void getPrompt_ShouldReturnPersistedPromptById()
            throws Exception {

        AiPrompt savedPrompt = repository.save(
                AiPrompt.builder()
                        .feature(FEATURE)
                        .type(TYPE)
                        .targetModel(TARGET_MODEL)
                        .version(3L)
                        .content("Persisted prompt content")
                        .active(false)
                        .createdByUserId("system")
                        .createdByUsername("system")
                        .createdAt(Instant.now())
                        .promptDescription("Persisted prompt")
                        .versionComment("Version 3")
                        .build()
        );

        mockMvc.perform(get(PROMPT_BY_ID_URL, savedPrompt.id()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.id")
                        .value(savedPrompt.id()))
                .andExpect(jsonPath("$.feature")
                        .value(FEATURE.name()))
                .andExpect(jsonPath("$.type")
                        .value(TYPE.name()))
                .andExpect(jsonPath("$.targetModel")
                        .value(TARGET_MODEL.name()))
                .andExpect(jsonPath("$.version")
                        .value(3))
                .andExpect(jsonPath("$.content")
                        .value("Persisted prompt content"))
                .andExpect(jsonPath("$.active")
                        .value(false))
                .andExpect(jsonPath("$.description")
                        .value("Persisted prompt"))
                .andExpect(jsonPath("$.versionComment")
                        .value("Version 3"))
                .andExpect(jsonPath("$.createdByUserId")
                        .value("system"))
                .andExpect(jsonPath("$.createdByUsername")
                        .value("system"))
                .andExpect(jsonPath("$.createdAt")
                        .isNotEmpty());
    }

    @Test
    void getPrompt_ShouldReturn404_WhenPromptDoesNotExist()
            throws Exception {

        String missingPromptId =
                "64f123456789abcdef123456";

        mockMvc.perform(get(PROMPT_BY_ID_URL, missingPromptId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.status")
                        .value(404))
                .andExpect(jsonPath("$.error")
                        .value(ErrorCode.AI_PROMPT_NOT_FOUND.name()))
                .andExpect(jsonPath("$.path")
                        .value(PROMPT_BY_ID_URL
                                .replace("{promptId}", missingPromptId)));
    }

    @Test
    void getPromptVersions_ShouldReturnVersionsOrderedByVersionDescending()
            throws Exception {

        repository.save(
                createPrompt(
                        null,
                        1L,
                        false,
                        "Prompt version 1",
                        "Version 1"
                )
        );

        repository.save(
                createPrompt(
                        null,
                        3L,
                        true,
                        "Prompt version 3",
                        "Version 3"
                )
        );

        repository.save(
                createPrompt(
                        null,
                        2L,
                        false,
                        "Prompt version 2",
                        "Version 2"
                )
        );

        mockMvc.perform(get(PROMPTS_URL)
                        .param("feature", FEATURE.name())
                        .param("type", TYPE.name())
                        .param("targetModel", TARGET_MODEL.name()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.length()")
                        .value(3))
                .andExpect(jsonPath("$[0].version")
                        .value(3))
                .andExpect(jsonPath("$[0].active")
                        .value(true))
                .andExpect(jsonPath("$[1].version")
                        .value(2))
                .andExpect(jsonPath("$[1].active")
                        .value(false))
                .andExpect(jsonPath("$[2].version")
                        .value(1))
                .andExpect(jsonPath("$[2].active")
                        .value(false));
    }

    @Test
    void getPromptVersions_ShouldReturnEmptyList_WhenNoVersionsExist()
            throws Exception {

        mockMvc.perform(get(PROMPTS_URL)
                        .param("feature", FEATURE.name())
                        .param("type", TYPE.name())
                        .param("targetModel", TARGET_MODEL.name()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.length()")
                        .value(0));
    }

    @Test
    void getPromptVersions_ShouldNormalizeEnumQueryParameters()
            throws Exception {

        repository.save(
                createPrompt(
                        null,
                        1L,
                        false,
                        "Prompt version 1",
                        "Version 1"
                )
        );

        mockMvc.perform(get(PROMPTS_URL)
                        .param("feature", "medical-summary")
                        .param("type", "system")
                        .param("targetModel", "llama_3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                        .value(1))
                .andExpect(jsonPath("$[0].feature")
                        .value(FEATURE.name()))
                .andExpect(jsonPath("$[0].type")
                        .value(TYPE.name()))
                .andExpect(jsonPath("$[0].targetModel")
                        .value(TARGET_MODEL.name()));
    }

    @Test
    void getCurrentPrompt_ShouldReturnActivePrompt()
            throws Exception {

        repository.save(
                createPrompt(
                        null,
                        1L,
                        false,
                        "Inactive prompt",
                        "Version 1"
                )
        );

        AiPrompt activePrompt = repository.save(
                createPrompt(
                        null,
                        2L,
                        true,
                        "Current active prompt",
                        "Version 2"
                )
        );

        mockMvc.perform(get(CURRENT_PROMPT_URL)
                        .param("feature", FEATURE.name())
                        .param("type", TYPE.name())
                        .param("targetModel", TARGET_MODEL.name()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.id")
                        .value(activePrompt.id()))
                .andExpect(jsonPath("$.feature")
                        .value(FEATURE.name()))
                .andExpect(jsonPath("$.type")
                        .value(TYPE.name()))
                .andExpect(jsonPath("$.targetModel")
                        .value(TARGET_MODEL.name()))
                .andExpect(jsonPath("$.version")
                        .value(2))
                .andExpect(jsonPath("$.content")
                        .value("Current active prompt"))
                .andExpect(jsonPath("$.active")
                        .value(true));
    }

    @Test
    void getCurrentPrompt_ShouldReturn404_WhenActivePromptDoesNotExist()
            throws Exception {

        repository.save(
                createPrompt(
                        null,
                        1L,
                        false,
                        "Inactive prompt",
                        "Version 1"
                )
        );

        mockMvc.perform(get(CURRENT_PROMPT_URL)
                        .param("feature", FEATURE.name())
                        .param("type", TYPE.name())
                        .param("targetModel", TARGET_MODEL.name()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.error")
                        .value(ErrorCode.AI_PROMPT_NOT_FOUND.name()))
                .andExpect(jsonPath("$.status")
                        .value(404));
    }

    @Test
    void getCurrentPrompt_ShouldReturn409_WhenMultipleActivePromptsExist()
            throws Exception {

        repository.save(
                createPrompt(
                        null,
                        1L,
                        true,
                        "Active prompt version 1",
                        "Version 1"
                )
        );

        repository.save(
                createPrompt(
                        null,
                        2L,
                        true,
                        "Active prompt version 2",
                        "Version 2"
                )
        );

        mockMvc.perform(get(CURRENT_PROMPT_URL)
                        .param("feature", FEATURE.name())
                        .param("type", TYPE.name())
                        .param("targetModel", TARGET_MODEL.name()))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.error")
                        .value(ErrorCode.AI_PROMPT_VERSION_CONFLICT.name()))
                .andExpect(jsonPath("$.status")
                        .value(409));
    }

    @Test
    void activatePrompt_ShouldActivateSelectedPrompt_DeactivatePreviousAndEvictCache()
            throws Exception {

        AiPrompt oldActivePrompt = repository.save(
                createPrompt(
                        null,
                        1L,
                        true,
                        "Old active prompt",
                        "Version 1"
                )
        );

        AiPrompt newInactivePrompt = repository.save(
                createPrompt(
                        null,
                        2L,
                        false,
                        "New prompt",
                        "Version 2"
                )
        );

        AiPromptKey promptKey = new AiPromptKey(
                FEATURE,
                TYPE,
                TARGET_MODEL
        );

        Cache cache = getActivePromptCache();
        String cacheKey = PromptCacheKey.of(promptKey);

        AiPrompt cachedPrompt =
                cachedActivePromptService.findActivePrompt(promptKey);

        assertThat(cachedPrompt)
                .isNotNull();

        assertThat(cachedPrompt.id())
                .isEqualTo(oldActivePrompt.id());

        assertThat(cache.get(cacheKey))
                .as("Old active prompt must be present in cache before activation")
                .isNotNull();

        mockMvc.perform(
                        patch(ACTIVATE_PROMPT_URL, newInactivePrompt.id())
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.id")
                        .value(newInactivePrompt.id()))
                .andExpect(jsonPath("$.feature")
                        .value(FEATURE.name()))
                .andExpect(jsonPath("$.type")
                        .value(TYPE.name()))
                .andExpect(jsonPath("$.targetModel")
                        .value(TARGET_MODEL.name()))
                .andExpect(jsonPath("$.version")
                        .value(2))
                .andExpect(jsonPath("$.content")
                        .value("New prompt"))
                .andExpect(jsonPath("$.active")
                        .value(true))
                .andExpect(jsonPath("$.updatedByUserId")
                        .value("system"))
                .andExpect(jsonPath("$.updatedByUsername")
                        .value("system"))
                .andExpect(jsonPath("$.updatedAt")
                        .isNotEmpty());

        AiPrompt oldPromptAfterActivation =
                repository.findById(oldActivePrompt.id())
                        .orElseThrow();

        AiPrompt newPromptAfterActivation =
                repository.findById(newInactivePrompt.id())
                        .orElseThrow();

        assertThat(oldPromptAfterActivation.active())
                .isFalse();

        assertThat(oldPromptAfterActivation.updatedByUserId())
                .isEqualTo("system");

        assertThat(oldPromptAfterActivation.updatedByUsername())
                .isEqualTo("system");

        assertThat(oldPromptAfterActivation.updatedAt())
                .isNotNull();

        assertThat(newPromptAfterActivation.active())
                .isTrue();

        assertThat(newPromptAfterActivation.updatedByUserId())
                .isEqualTo("system");

        assertThat(newPromptAfterActivation.updatedByUsername())
                .isEqualTo("system");

        assertThat(newPromptAfterActivation.updatedAt())
                .isNotNull();

        List<AiPrompt> activePrompts =
                repository.findAllByFeatureAndTypeAndTargetModelAndActiveTrue(
                        FEATURE,
                        TYPE,
                        TARGET_MODEL
                );

        assertThat(activePrompts)
                .hasSize(1);

        assertThat(activePrompts.get(0).id())
                .isEqualTo(newInactivePrompt.id());

        assertThat(cache.get(cacheKey))
                .as("Cache entry must be evicted after activation commit")
                .isNull();
    }

    @Test
    void activatePrompt_ShouldReturn404_WhenPromptDoesNotExist()
            throws Exception {

        String missingPromptId =
                "64f123456789abcdef123456";

        mockMvc.perform(
                        patch(ACTIVATE_PROMPT_URL, missingPromptId)
                )
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.status")
                        .value(404));
    }

    @Test
    void activatePrompt_ShouldReturnNewCurrentPromptAndRepopulateCache()
            throws Exception {

        AiPrompt oldActivePrompt = repository.save(
                createPrompt(
                        null,
                        1L,
                        true,
                        "Old active prompt",
                        "Version 1"
                )
        );

        AiPrompt newInactivePrompt = repository.save(
                createPrompt(
                        null,
                        2L,
                        false,
                        "New active prompt",
                        "Version 2"
                )
        );

        AiPromptKey promptKey = new AiPromptKey(
                FEATURE,
                TYPE,
                TARGET_MODEL
        );

        Cache cache = getActivePromptCache();
        String cacheKey = PromptCacheKey.of(promptKey);

        AiPrompt cachedOldPrompt =
                cachedActivePromptService.findActivePrompt(promptKey);

        assertThat(cachedOldPrompt)
                .isNotNull();

        assertThat(cachedOldPrompt.id())
                .isEqualTo(oldActivePrompt.id());

        assertThat(cache.get(cacheKey))
                .as("Old active prompt must be cached before activation")
                .isNotNull();

        mockMvc.perform(
                        patch(
                                ACTIVATE_PROMPT_URL,
                                newInactivePrompt.id()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(newInactivePrompt.id()))
                .andExpect(jsonPath("$.version")
                        .value(2))
                .andExpect(jsonPath("$.active")
                        .value(true));

        assertThat(cache.get(cacheKey))
                .as("Cache must be empty after activation eviction")
                .isNull();

        mockMvc.perform(get(CURRENT_PROMPT_URL)
                        .param("feature", FEATURE.name())
                        .param("type", TYPE.name())
                        .param("targetModel", TARGET_MODEL.name()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.id")
                        .value(newInactivePrompt.id()))
                .andExpect(jsonPath("$.feature")
                        .value(FEATURE.name()))
                .andExpect(jsonPath("$.type")
                        .value(TYPE.name()))
                .andExpect(jsonPath("$.targetModel")
                        .value(TARGET_MODEL.name()))
                .andExpect(jsonPath("$.version")
                        .value(2))
                .andExpect(jsonPath("$.content")
                        .value("New active prompt"))
                .andExpect(jsonPath("$.active")
                        .value(true));

        AiPrompt refreshedPrompt =
                cachedActivePromptService.findActivePrompt(promptKey);

        assertThat(refreshedPrompt)
                .isNotNull();

        assertThat(refreshedPrompt.id())
                .isEqualTo(newInactivePrompt.id());

        assertThat(refreshedPrompt.version())
                .isEqualTo(2L);

        assertThat(refreshedPrompt.content())
                .isEqualTo("New active prompt");

        Cache.ValueWrapper refreshedCachedValue =
                cache.get(cacheKey);

        assertThat(refreshedCachedValue)
                .as("New active prompt must be cached after refresh")
                .isNotNull();

        AiPrompt promptFromCache =
                cache.get(cacheKey, AiPrompt.class);

        assertThat(promptFromCache)
                .isNotNull();

        assertThat(promptFromCache.id())
                .isEqualTo(newInactivePrompt.id());

        assertThat(promptFromCache.version())
                .isEqualTo(2L);

        assertThat(promptFromCache.content())
                .isEqualTo("New active prompt");
    }

    @Test
    void createPrompt_ShouldReturn400_WhenRequestBodyIsInvalid()
            throws Exception {

        String invalidRequest = """
                {
                  "feature": null,
                  "type": null,
                  "targetModel": null,
                  "content": "   ",
                  "promptDescription": "Description",
                  "versionComment": "Comment"
                }
                """;

        mockMvc.perform(post(PROMPTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.error")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message")
                        .value("Validation failed"))
                .andExpect(jsonPath("$.path")
                        .value(PROMPTS_URL))
                .andExpect(jsonPath("$.validationErrors")
                        .isArray())
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors[?(@.field=='feature')].message")
                        .value(hasItem("Feature must not be null")))
                .andExpect(jsonPath("$.validationErrors[?(@.field=='type')].message")
                        .value(hasItem("Prompt type must not be null")))
                .andExpect(jsonPath("$.validationErrors[?(@.field=='targetModel')].message")
                        .value(hasItem("Target model must not be null")))
                .andExpect(jsonPath("$.validationErrors[?(@.field=='content')].message")
                        .value(hasItem("Content must not be empty")))
                .andExpect(jsonPath("$.validationErrors[?(@.field=='content')].message")
                        .value(hasItem("Content length must be between 10 and 30000 characters")));

        assertThat(repository.count())
                .isZero();
    }

    @Test
    void createPrompt_ShouldReturn400_WhenRequestBodyIsMissing()
            throws Exception {

        mockMvc.perform(post(PROMPTS_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.path")
                        .value(PROMPTS_URL));

        assertThat(repository.count())
                .isZero();
    }

    @Test
    void createPrompt_ShouldReturn400_WhenJsonIsMalformed()
            throws Exception {

        String malformedJson = """
                {
                  "feature": "MEDICAL_SUMMARY",
                  "type": "SYSTEM",
                  "targetModel": "LLAMA_3",
                  "content": "Valid prompt content",
                }
                """;

        mockMvc.perform(post(PROMPTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.path")
                        .value(PROMPTS_URL));

        assertThat(repository.count())
                .isZero();
    }

    @Test
    void getPromptVersions_ShouldReturn400_WhenFeatureParameterIsMissing()
            throws Exception {

        mockMvc.perform(get(PROMPTS_URL)
                        .param("type", TYPE.name())
                        .param("targetModel", TARGET_MODEL.name()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.path")
                        .value(PROMPTS_URL));
    }

    @Test
    void getPromptVersions_ShouldReturn400_WhenFeatureParameterIsBlank()
            throws Exception {

        mockMvc.perform(get(PROMPTS_URL)
                        .param("feature", "")
                        .param("type", TYPE.name())
                        .param("targetModel", TARGET_MODEL.name()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.path")
                        .value(PROMPTS_URL));
    }

    @Test
    void getPromptVersions_ShouldReturn400_WhenFeatureEnumIsInvalid()
            throws Exception {

        mockMvc.perform(get(PROMPTS_URL)
                        .param("feature", "UNKNOWN_FEATURE")
                        .param("type", TYPE.name())
                        .param("targetModel", TARGET_MODEL.name()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.path")
                        .value(PROMPTS_URL));
    }

    @Test
    void createPrompt_ShouldPreserveUniqueVersions_WhenRequestsAreConcurrent()
            throws Exception {

        int requestCount = 10;

        ExecutorService executor =
                Executors.newFixedThreadPool(requestCount);

        CountDownLatch readyLatch =
                new CountDownLatch(requestCount);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        try {
            List<Future<Integer>> futures =
                    new ArrayList<>();

            for (int index = 0; index < requestCount; index++) {
                int requestNumber = index;

                futures.add(
                        executor.submit(() -> {
                            CreateAiPromptRequest request =
                                    new CreateAiPromptRequest(
                                            FEATURE,
                                            TYPE,
                                            TARGET_MODEL,
                                            """
                                                    You are a medical summary assistant.
                                                    
                                                    Return valid JSON only.
                                                    Concurrent request number %d.
                                                    """.formatted(requestNumber),
                                            "Concurrent prompt",
                                            "Concurrent version " + requestNumber
                                    );

                            readyLatch.countDown();

                            boolean started =
                                    startLatch.await(
                                            10,
                                            TimeUnit.SECONDS
                                    );

                            assertThat(started)
                                    .as("All concurrent tasks must receive the start signal")
                                    .isTrue();

                            MvcResult result =
                                    mockMvc.perform(
                                                    post(PROMPTS_URL)
                                                            .contentType(
                                                                    MediaType.APPLICATION_JSON
                                                            )
                                                            .content(
                                                                    objectMapper.writeValueAsString(
                                                                            request
                                                                    )
                                                            )
                                            )
                                            .andReturn();

                            return result.getResponse().getStatus();
                        })
                );
            }

            boolean allThreadsReady =
                    readyLatch.await(
                            10,
                            TimeUnit.SECONDS
                    );

            assertThat(allThreadsReady)
                    .as("All concurrent requests must be ready before execution")
                    .isTrue();

            startLatch.countDown();

            List<Integer> statuses =
                    new ArrayList<>();

            for (Future<Integer> future : futures) {
                statuses.add(
                        future.get(
                                30,
                                TimeUnit.SECONDS
                        )
                );
            }

            assertThat(statuses)
                    .allMatch(status ->
                            status == HttpStatus.CREATED.value()
                                    || status == HttpStatus.CONFLICT.value()
                    );

            assertThat(statuses)
                    .contains(HttpStatus.CREATED.value());

            List<AiPrompt> savedPrompts =
                    repository
                            .findByFeatureAndTypeAndTargetModelOrderByVersionDesc(
                                    FEATURE,
                                    TYPE,
                                    TARGET_MODEL
                            );

            assertThat(savedPrompts)
                    .isNotEmpty();

            Set<Long> uniqueVersions =
                    new HashSet<>();

            for (AiPrompt prompt : savedPrompts) {
                assertThat(prompt.version())
                        .isPositive();

                assertThat(prompt.active())
                        .isFalse();

                boolean added =
                        uniqueVersions.add(prompt.version());

                assertThat(added)
                        .as(
                                "Duplicate prompt version detected: %s",
                                prompt.version()
                        )
                        .isTrue();
            }

            assertThat(uniqueVersions)
                    .hasSize(savedPrompts.size());

            assertThat(repository.count())
                    .isEqualTo(savedPrompts.size());

        } finally {
            executor.shutdownNow();

            boolean terminated =
                    executor.awaitTermination(
                            10,
                            TimeUnit.SECONDS
                    );

            assertThat(terminated)
                    .as("Executor must terminate after concurrency test")
                    .isTrue();
        }
    }

    @Test
    void aiPromptCollection_ShouldHaveUniqueVersionIndex() {
        List<IndexInfo> indexes =
                mongoTemplate.indexOps(AiPrompt.class)
                        .getIndexInfo();

        IndexInfo versionIndex = indexes.stream()
                .filter(index ->
                        "feature_type_model_version_idx"
                                .equals(index.getName())
                )
                .findFirst()
                .orElseThrow();

        assertThat(versionIndex.isUnique())
                .isTrue();

        assertThat(versionIndex.getIndexFields())
                .extracting(IndexField::getKey)
                .containsExactly(
                        "feature",
                        "type",
                        "targetModel",
                        "version"
                );
    }

    @Test
    void activatePrompt_ShouldPreserveSingleActivePrompt_WhenRequestsAreConcurrent()
            throws Exception {

        AiPrompt initialActivePrompt = repository.save(
                createPrompt(
                        null,
                        1L,
                        true,
                        "Initial active prompt",
                        "Version 1"
                )
        );

        AiPrompt secondPrompt = repository.save(
                createPrompt(
                        null,
                        2L,
                        false,
                        "Second prompt",
                        "Version 2"
                )
        );

        AiPrompt thirdPrompt = repository.save(
                createPrompt(
                        null,
                        3L,
                        false,
                        "Third prompt",
                        "Version 3"
                )
        );

        AiPromptKey promptKey = new AiPromptKey(
                FEATURE,
                TYPE,
                TARGET_MODEL
        );

        Cache cache = getActivePromptCache();
        String cacheKey = PromptCacheKey.of(promptKey);

        AiPrompt cachedPrompt =
                cachedActivePromptService.findActivePrompt(promptKey);

        assertThat(cachedPrompt)
                .isNotNull();

        assertThat(cachedPrompt.id())
                .isEqualTo(initialActivePrompt.id());

        assertThat(cache.get(cacheKey))
                .as("Initial active prompt must be cached before concurrent activation")
                .isNotNull();

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        CountDownLatch readyLatch =
                new CountDownLatch(2);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        try {
            Future<Integer> secondPromptActivation =
                    executor.submit(() ->
                            activatePromptConcurrently(
                                    secondPrompt.id(),
                                    readyLatch,
                                    startLatch
                            )
                    );

            Future<Integer> thirdPromptActivation =
                    executor.submit(() ->
                            activatePromptConcurrently(
                                    thirdPrompt.id(),
                                    readyLatch,
                                    startLatch
                            )
                    );

            boolean bothRequestsReady =
                    readyLatch.await(
                            10,
                            TimeUnit.SECONDS
                    );

            assertThat(bothRequestsReady)
                    .as("Both activation requests must be ready before execution")
                    .isTrue();

            startLatch.countDown();

            int secondPromptStatus =
                    secondPromptActivation.get(
                            30,
                            TimeUnit.SECONDS
                    );

            int thirdPromptStatus =
                    thirdPromptActivation.get(
                            30,
                            TimeUnit.SECONDS
                    );

            List<Integer> statuses = List.of(
                    secondPromptStatus,
                    thirdPromptStatus
            );

            assertThat(statuses)
                    .allMatch(status ->
                            status == HttpStatus.OK.value()
                                    || status == HttpStatus.CONFLICT.value()
                    );

            assertThat(statuses)
                    .doesNotContain(
                            HttpStatus.INTERNAL_SERVER_ERROR.value()
                    );


            assertThat(statuses)
                    .contains(HttpStatus.OK.value());

            List<AiPrompt> activePrompts =
                    repository
                            .findAllByFeatureAndTypeAndTargetModelAndActiveTrue(
                                    FEATURE,
                                    TYPE,
                                    TARGET_MODEL
                            );

            assertThat(activePrompts)
                    .as("Exactly one active prompt must remain after concurrent activation")
                    .hasSize(1);

            AiPrompt finalActivePrompt =
                    activePrompts.get(0);

            assertThat(finalActivePrompt.id())
                    .as("One of the concurrently selected prompts must become active")
                    .isIn(
                            secondPrompt.id(),
                            thirdPrompt.id()
                    );

            assertThat(finalActivePrompt.active())
                    .isTrue();

            AiPrompt initialPromptAfterActivation =
                    repository.findById(initialActivePrompt.id())
                            .orElseThrow();

            assertThat(initialPromptAfterActivation.active())
                    .isFalse();

            AiPrompt secondPromptAfterActivation =
                    repository.findById(secondPrompt.id())
                            .orElseThrow();

            AiPrompt thirdPromptAfterActivation =
                    repository.findById(thirdPrompt.id())
                            .orElseThrow();

            long activeConcurrentPromptsCount =
                    List.of(
                                    secondPromptAfterActivation,
                                    thirdPromptAfterActivation
                            )
                            .stream()
                            .filter(AiPrompt::active)
                            .count();

            assertThat(activeConcurrentPromptsCount)
                    .as("Only one concurrently selected prompt may remain active")
                    .isEqualTo(1L);

            assertThat(cache.get(cacheKey))
                    .as("Active prompt cache must be evicted after successful activation")
                    .isNull();


            mockMvc.perform(get(CURRENT_PROMPT_URL)
                            .param("feature", FEATURE.name())
                            .param("type", TYPE.name())
                            .param("targetModel", TARGET_MODEL.name()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(
                            MediaType.APPLICATION_JSON
                    ))
                    .andExpect(jsonPath("$.id")
                            .value(finalActivePrompt.id()))
                    .andExpect(jsonPath("$.feature")
                            .value(FEATURE.name()))
                    .andExpect(jsonPath("$.type")
                            .value(TYPE.name()))
                    .andExpect(jsonPath("$.targetModel")
                            .value(TARGET_MODEL.name()))
                    .andExpect(jsonPath("$.active")
                            .value(true));

            AiPrompt refreshedPrompt =
                    cachedActivePromptService.findActivePrompt(promptKey);

            assertThat(refreshedPrompt)
                    .isNotNull();

            assertThat(refreshedPrompt.id())
                    .isEqualTo(finalActivePrompt.id());

            assertThat(refreshedPrompt.active())
                    .isTrue();

            assertThat(cache.get(cacheKey))
                    .as("Final active prompt must be cached after refresh")
                    .isNotNull();

            AiPrompt promptFromCache =
                    cache.get(cacheKey, AiPrompt.class);

            assertThat(promptFromCache)
                    .isNotNull();

            assertThat(promptFromCache.id())
                    .isEqualTo(finalActivePrompt.id());

        } finally {
            executor.shutdownNow();

            boolean terminated =
                    executor.awaitTermination(
                            10,
                            TimeUnit.SECONDS
                    );

            assertThat(terminated)
                    .as("Executor must terminate after the concurrency test")
                    .isTrue();
        }
    }

    private int activatePromptConcurrently(
            String promptId,
            CountDownLatch readyLatch,
            CountDownLatch startLatch
    ) throws Exception {

        readyLatch.countDown();

        boolean started =
                startLatch.await(
                        10,
                        TimeUnit.SECONDS
                );

        if (!started) {
            throw new IllegalStateException(
                    "Concurrent activation did not receive start signal"
            );
        }

        MvcResult result =
                mockMvc.perform(
                                patch(
                                        ACTIVATE_PROMPT_URL,
                                        promptId
                                )
                        )
                        .andReturn();

        return result.getResponse().getStatus();
    }

    private AiPrompt createPrompt(
            String id,
            long version,
            boolean active,
            String content,
            String versionComment
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
                .createdAt(Instant.now())
                .updatedByUserId(active ? "system" : null)
                .updatedByUsername(active ? "system" : null)
                .updatedAt(active ? Instant.now() : null)
                .promptDescription("Medical Summary System Prompt")
                .versionComment(versionComment)
                .build();
    }

    private CreateAiPromptRequest createPromptRequest() {
        return new CreateAiPromptRequest(
                FEATURE,
                TYPE,
                TARGET_MODEL,
                PROMPT_CONTENT,
                PROMPT_DESCRIPTION,
                VERSION_COMMENT
        );
    }

    private Cache getActivePromptCache() {
        Cache cache =
                cacheManager.getCache(CacheNames.ACTIVE_PROMPTS);

        assertThat(cache)
                .as("Active prompt cache must be configured")
                .isNotNull();

        return cache;
    }
}
