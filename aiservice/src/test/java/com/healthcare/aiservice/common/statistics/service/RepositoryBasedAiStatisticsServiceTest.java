package com.healthcare.aiservice.common.statistics.service;

import com.healthcare.aiservice.common.provider.logging.AiRequestStatus;
import com.healthcare.aiservice.common.statistics.dto.AiStatisticsResponse;
import com.healthcare.aiservice.config.constant.FeatureName;
import com.healthcare.aiservice.repository.AiRequestLogRepository;
import com.healthcare.aiservice.repository.projection.AverageDurationProjection;
import com.healthcare.aiservice.repository.projection.FeatureCountProjection;
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
import static org.mockito.Mockito.when;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Repository based AI statistics service tests: ")
@ExtendWith(MockitoExtension.class)
class RepositoryBasedAiStatisticsServiceTest {

    @Mock
    private AiRequestLogRepository repository;

    @InjectMocks
    private RepositoryBasedAiStatisticsService service;

    @Test
    void getStatistic_ShouldReturnEmptyStatistics_WhenRepositoryHasNoLogs() {
        when(repository.count()).thenReturn(0L);
        when(repository.countByStatus(AiRequestStatus.SUCCESS)).thenReturn(0L);
        when(repository.countByStatus(AiRequestStatus.FAILED)).thenReturn(0L);
        when(repository.findAverageDuration()).thenReturn(null);
        when(repository.countRequestsGroupedByFeature()).thenReturn(List.of());

        AiStatisticsResponse result = service.getStatistic();

        assertThat(result.totalRequests()).isZero();
        assertThat(result.successfulRequests()).isZero();
        assertThat(result.failedRequests()).isZero();
        assertThat(result.averageDurationMs()).isZero();

        assertThat(result.requestsByFeature())
                .hasSize(FeatureName.values().length)
                .allSatisfy(stat -> assertThat(stat.requests()).isZero());
    }

    @Test
    void getStatistic_ShouldReturnStatistics_WhenRepositoryHasLogs() {
        AverageDurationProjection averageProjection = () -> 843.4;

        FeatureCountProjection summaryProjection = new FeatureCountProjection() {
            @Override
            public FeatureName getFeature() {
                return FeatureName.MEDICAL_SUMMARY;
            }

            @Override
            public long getCount() {
                return 50L;
            }
        };

        FeatureCountProjection classificationProjection = new FeatureCountProjection() {
            @Override
            public FeatureName getFeature() {
                return FeatureName.MESSAGE_CLASSIFICATION;
            }

            @Override
            public long getCount() {
                return 40L;
            }
        };

        when(repository.count()).thenReturn(125L);
        when(repository.countByStatus(AiRequestStatus.SUCCESS)).thenReturn(120L);
        when(repository.countByStatus(AiRequestStatus.FAILED)).thenReturn(5L);
        when(repository.findAverageDuration()).thenReturn(averageProjection);
        when(repository.countRequestsGroupedByFeature())
                .thenReturn(List.of(summaryProjection, classificationProjection));

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
                });
    }
}