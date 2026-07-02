package com.healthcare.aiservice.common.statistics.contriller;


import com.healthcare.aiservice.common.statistics.dto.AiStatisticsResponse;
import com.healthcare.aiservice.common.statistics.dto.FeatureStatistics;
import com.healthcare.aiservice.common.statistics.service.interfaces.AiStatisticService;
import com.healthcare.aiservice.config.constant.FeatureName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.healthcare.aiservice.common.statistics.contriller.API.AiStatisticsApiPaths.STATISTICS_ADMIN_URL;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiStatisticsController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("AI statistics controller tests: ")
class AiStatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AiStatisticService statisticsService;

    @Test
    void getStatistics_ShouldReturnAiStatistics() throws Exception {
        AiStatisticsResponse response = AiStatisticsResponse.builder()
                .totalRequests(22L)
                .successfulRequests(12L)
                .failedRequests(10L)
                .averageDurationMs(48750L)
                .requestsByFeature(List.of(
                        FeatureStatistics.builder()
                                .feature(FeatureName.MEDICAL_SUMMARY)
                                .requests(8L)
                                .build(),
                        FeatureStatistics.builder()
                                .feature(FeatureName.MEDICAL_EXTRACTION)
                                .requests(6L)
                                .build(),
                        FeatureStatistics.builder()
                                .feature(FeatureName.MESSAGE_CLASSIFICATION)
                                .requests(8L)
                                .build()
                ))
                .build();

        when(statisticsService.getStatistic()).thenReturn(response);

        mockMvc.perform(get(STATISTICS_ADMIN_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRequests").value(22))
                .andExpect(jsonPath("$.successfulRequests").value(12))
                .andExpect(jsonPath("$.failedRequests").value(10))
                .andExpect(jsonPath("$.averageDurationMs").value(48750))
                .andExpect(jsonPath("$.requestsByFeature[0].feature").value(FeatureName.MEDICAL_SUMMARY.name()))
                .andExpect(jsonPath("$.requestsByFeature[0].requests").value(8))
                .andExpect(jsonPath("$.requestsByFeature[1].feature").value(FeatureName.MEDICAL_EXTRACTION.name()))
                .andExpect(jsonPath("$.requestsByFeature[1].requests").value(6))
                .andExpect(jsonPath("$.requestsByFeature[2].feature").value(FeatureName.MESSAGE_CLASSIFICATION.name()))
                .andExpect(jsonPath("$.requestsByFeature[2].requests").value(8));
    }
}