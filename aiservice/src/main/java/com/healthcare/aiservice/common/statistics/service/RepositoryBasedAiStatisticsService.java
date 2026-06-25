package com.healthcare.aiservice.common.statistics.service;

import com.healthcare.aiservice.common.provider.logging.AiRequestStatus;
import com.healthcare.aiservice.common.statistics.dto.AiStatisticsResponse;
import com.healthcare.aiservice.common.statistics.dto.FeatureStatistics;
import com.healthcare.aiservice.common.statistics.mapper.FeatureStatisticsMapper;
import com.healthcare.aiservice.common.statistics.service.interfaces.AiStatisticService;
import com.healthcare.aiservice.config.constant.FeatureName;
import com.healthcare.aiservice.repository.AiRequestLogRepository;
import com.healthcare.aiservice.repository.projection.AverageDurationProjection;
import com.healthcare.aiservice.repository.projection.FeatureCountProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Primary
@RequiredArgsConstructor
public class RepositoryBasedAiStatisticsService implements AiStatisticService {

    private final AiRequestLogRepository repository;

    @Override
    public AiStatisticsResponse getStatistic() {
        return AiStatisticsResponse.builder()
                .totalRequests(repository.count())
                .successfulRequests(repository.countByStatus(AiRequestStatus.SUCCESS))
                .failedRequests(repository.countByStatus(AiRequestStatus.FAILED))
                .averageDurationMs(resolveAverageDuration())
                .requestsByFeature(resolveRequestsByFeature())
                .build();
    }

    private long resolveAverageDuration() {
        AverageDurationProjection projection = repository.findAverageDuration();

        if (projection == null || projection.getAverageDurationMs() == null) {
            return 0L;
        }

        return Math.round(projection.getAverageDurationMs());
    }

    private List<FeatureStatistics> resolveRequestsByFeature() {
        Map<FeatureName, Long> statistics =
                FeatureStatisticsMapper.initializeFeatureStatisticsMap();

        List<FeatureCountProjection> projections =
                repository.countRequestsGroupedByFeature();

        if (projections == null || projections.isEmpty()) {
            return FeatureStatisticsMapper.empty();
        }

        projections.forEach(projection -> applyFeatureCount(statistics, projection));

        return FeatureStatisticsMapper.toFeatureStatisticsList(statistics);
    }

    private void applyFeatureCount(
            Map<FeatureName, Long> statistics,
            FeatureCountProjection projection
    ) {
        if (projection == null || projection.getFeature() == null) {
            return;
        }

        statistics.put(
                projection.getFeature(),
                projection.getCount()
        );
    }
}