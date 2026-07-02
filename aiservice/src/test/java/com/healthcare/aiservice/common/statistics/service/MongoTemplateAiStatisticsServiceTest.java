package com.healthcare.aiservice.common.statistics.service;



import com.healthcare.aiservice.common.statistics.dto.AiStatisticsResponse;
import com.healthcare.aiservice.config.constant.FeatureName;
import com.healthcare.aiservice.repository.MongoTemplateAiStatisticsRepository;
import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.healthcare.aiservice.common.statistics.constnats.AiStatisticsAggregationConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Mongo template based AI statistics service tests: ")
@ExtendWith(MockitoExtension.class)
class MongoTemplateAiStatisticsServiceTest {

    @Mock
    private MongoTemplateAiStatisticsRepository repository;

    @InjectMocks
    private MongoTemplateAiStatisticsService service;

    @Test
    void getStatistic_ShouldReturnEmptyStatistics_WhenRepositoryReturnsNull() {
        when(repository.getAiStatistics()).thenReturn(null);

        AiStatisticsResponse result = service.getStatistic();

        assertThat(result.totalRequests()).isZero();
        assertThat(result.successfulRequests()).isZero();
        assertThat(result.failedRequests()).isZero();
        assertThat(result.averageDurationMs()).isZero();

        assertThat(result.requestsByFeature())
                .hasSize(FeatureName.values().length)
                .allSatisfy(feature -> assertThat(feature.requests()).isZero());
    }

    @Test
    void getStatistic_ShouldReturnStatistics_WhenAggregationResultExists() {
        Document aggregationResult = new Document()
                .append(FACET_TOTAL, List.of(
                        new Document(KEY_COUNT, 125L)
                ))
                .append(FACET_SUCCESSFUL, List.of(
                        new Document(KEY_COUNT, 120L)
                ))
                .append(FACET_FAILED, List.of(
                        new Document(KEY_COUNT, 5L)
                ))
                .append(FACET_AVERAGE, List.of(
                        new Document(KEY_AVERAGE_DURATION_MS, 843.4)
                ))
                .append(FACET_BY_FEATURE, List.of(
                        new Document(KEY_ID, FeatureName.MEDICAL_SUMMARY.name())
                                .append(KEY_COUNT, 50L),
                        new Document(KEY_ID, FeatureName.MESSAGE_CLASSIFICATION.name())
                                .append(KEY_COUNT, 40L)
                ));

        when(repository.getAiStatistics()).thenReturn(aggregationResult);

        AiStatisticsResponse result = service.getStatistic();

        assertThat(result.totalRequests()).isEqualTo(125L);
        assertThat(result.successfulRequests()).isEqualTo(120L);
        assertThat(result.failedRequests()).isEqualTo(5L);
        assertThat(result.averageDurationMs()).isEqualTo(843L);

        assertThat(result.requestsByFeature())
                .anySatisfy(stat -> {
                    assertThat(stat.feature()).isEqualTo(FeatureName.MEDICAL_SUMMARY);
                    assertThat(stat.requests()).isEqualTo(50L);
                })
                .anySatisfy(stat -> {
                    assertThat(stat.feature()).isEqualTo(FeatureName.MESSAGE_CLASSIFICATION);
                    assertThat(stat.requests()).isEqualTo(40L);
                })
                .anySatisfy(stat -> {
                    assertThat(stat.feature()).isEqualTo(FeatureName.MEDICAL_EXTRACTION);
                    assertThat(stat.requests()).isZero();
                });
    }

    @Test
    void getStatistic_ShouldIgnoreUnknownFeatureNames() {
        Document aggregationResult = new Document()
                .append(FACET_TOTAL, List.of(new Document(KEY_COUNT, 1L)))
                .append(FACET_SUCCESSFUL, List.of(new Document(KEY_COUNT, 1L)))
                .append(FACET_FAILED, List.of(new Document(KEY_COUNT, 0L)))
                .append(FACET_AVERAGE, List.of(new Document(KEY_AVERAGE_DURATION_MS, 100.0)))
                .append(FACET_BY_FEATURE, List.of(
                        new Document(KEY_ID, "UNKNOWN_FEATURE")
                                .append(KEY_COUNT, 99L)
                ));

        when(repository.getAiStatistics()).thenReturn(aggregationResult);

        AiStatisticsResponse result = service.getStatistic();

        assertThat(result.requestsByFeature())
                .hasSize(FeatureName.values().length)
                .allSatisfy(stat -> assertThat(stat.requests()).isZero());
    }

    @Test
    void getStatistic_ShouldUseZeroAverage_WhenAverageFacetIsEmpty() {
        Document aggregationResult = new Document()
                .append(FACET_TOTAL, List.of(new Document(KEY_COUNT, 1L)))
                .append(FACET_SUCCESSFUL, List.of(new Document(KEY_COUNT, 1L)))
                .append(FACET_FAILED, List.of(new Document(KEY_COUNT, 0L)))
                .append(FACET_AVERAGE, List.of())
                .append(FACET_BY_FEATURE, List.of());

        when(repository.getAiStatistics()).thenReturn(aggregationResult);

        AiStatisticsResponse result = service.getStatistic();

        assertThat(result.averageDurationMs()).isZero();
    }
}