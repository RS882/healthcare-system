package com.healthcare.aiservice.common.statistics.controller;

import com.healthcare.aiservice.common.provider.logging.AiRequestStatus;
import com.healthcare.aiservice.common.provider.logging.model.AiRequestLog;
import com.healthcare.aiservice.config.AbstractMongoDbIntegrationTest;
import com.healthcare.aiservice.config.constant.FeatureName;
import com.healthcare.aiservice.repository.AiRequestLogRepository;
import org.junit.jupiter.api.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;

import static com.healthcare.aiservice.common.statistics.controller.API.AiStatisticsApiPaths.STATISTICS_ADMIN_URL;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureMockMvc
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("AI statistics controller integration tests: ")
class AiStatisticsControllerIT extends AbstractMongoDbIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AiRequestLogRepository repository;

    @MockitoBean
    private ChatClient chatClient;

    @MockitoBean
    private DiscoveryClient discoveryClient;

    private static final String OLLAMA = "ollama";
    private static final String LLAMA_MODEL = "llama3:latest";
    private static final String EMPTY_JSON = "{}";

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        repository.save(AiRequestLog.builder()
                .feature(FeatureName.MEDICAL_SUMMARY)
                .provider(OLLAMA)
                .model(LLAMA_MODEL)
                .request(EMPTY_JSON)
                .response(EMPTY_JSON)
                .status(AiRequestStatus.SUCCESS)
                .durationMs(100L)
                .createdAt(Instant.now())
                .build());

        repository.save(AiRequestLog.builder()
                .feature(FeatureName.MEDICAL_SUMMARY)
                .provider(OLLAMA)
                .model(LLAMA_MODEL)
                .request(EMPTY_JSON)
                .response(EMPTY_JSON)
                .status(AiRequestStatus.SUCCESS)
                .durationMs(200L)
                .createdAt(Instant.now())
                .build());

        repository.save(AiRequestLog.builder()
                .feature(FeatureName.MESSAGE_CLASSIFICATION)
                .provider(OLLAMA)
                .model(LLAMA_MODEL)
                .request(EMPTY_JSON)
                .response(EMPTY_JSON)
                .status(AiRequestStatus.FAILED)
                .durationMs(300L)
                .errorType("JsonExtractorException")
                .errorMessage("JSON object not found")
                .createdAt(Instant.now())
                .build());
    }

    @Test
    void getStatistics_ShouldReturnStatisticsFromRealMongo() throws Exception {
        mockMvc.perform(get(STATISTICS_ADMIN_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRequests").value(3))
                .andExpect(jsonPath("$.successfulRequests").value(2))
                .andExpect(jsonPath("$.failedRequests").value(1))
                .andExpect(jsonPath("$.averageDurationMs").value(200))
                .andExpect(jsonPath(featureStatisticsJsonPath(FeatureName.MEDICAL_SUMMARY)).value(hasItem(2)))
                .andExpect(jsonPath(featureStatisticsJsonPath(FeatureName.MESSAGE_CLASSIFICATION)).value(hasItem(1)))
                .andExpect(jsonPath(featureStatisticsJsonPath(FeatureName.MEDICAL_EXTRACTION)).value(hasItem(0)));
    }

    private String featureStatisticsJsonPath(FeatureName feature) {
        return "requestsByFeature[?(@.feature=='" + feature + "')].requests";
    }
}
