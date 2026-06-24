package com.healthcare.aiservice.common.statistics.dto;

import com.healthcare.aiservice.config.constant.FeatureName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "Statistics by feature")
@Builder
public record FeatureStatistics(
        @Schema(description = "Name of feature", example = "MEDICAL_SUMMARY")
        FeatureName feature,

        @Schema(description = "Count of requests", example = "29")
        long requests
) {

    public static FeatureStatistics empty(FeatureName feature) {
        return FeatureStatistics.builder()
                .feature(feature)
                .requests(0L)
                .build();
    }
}
