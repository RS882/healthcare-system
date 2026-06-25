package com.healthcare.aiservice.common.statistics.dto;


import com.healthcare.aiservice.common.statistics.mapper.FeatureStatisticsMapper;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Schema(
        name = "AiStatisticsResponse",
        description = "AI service usage and performance statistics"
)
@Builder
public record AiStatisticsResponse(

        @Schema(example = "125")
        long totalRequests,

        @Schema(example = "120")
        long successfulRequests,

        @Schema(example = "5")
        long failedRequests,

        @Schema(example = "843")
        long averageDurationMs,

        @Schema(description = "Number of AI requests grouped by feature")
        @ArraySchema(
                schema = @Schema(implementation = FeatureStatistics.class)
        )
        List<FeatureStatistics> requestsByFeature
) {
    public static AiStatisticsResponse empty() {
        return AiStatisticsResponse.builder()
                .totalRequests(0L)
                .successfulRequests(0L)
                .failedRequests(0L)
                .averageDurationMs(0L)
                .requestsByFeature(FeatureStatisticsMapper.empty())
                .build();
    }
}
