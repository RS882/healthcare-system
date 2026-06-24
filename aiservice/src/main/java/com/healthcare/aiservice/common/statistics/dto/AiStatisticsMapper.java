package com.healthcare.aiservice.common.statistics.dto;

import com.healthcare.aiservice.config.constant.FeatureName;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class AiStatisticsMapper {

    private  AiStatisticsMapper() {
    }


    public static Map<FeatureName, Long> initializeFeatureStatisticsMap() {
        Map<FeatureName, Long> result = new EnumMap<>(FeatureName.class);
        Arrays.stream(FeatureName.values())
                .forEach(feature -> result.put(feature, 0L));
        return result;
    }

    public static List<FeatureStatistics> convertFeatureStatisticsMapToFeatureStatisticsList(Map<FeatureName, Long> map) {
        return map.entrySet().stream()
                .map(entry -> FeatureStatistics.builder()
                        .feature(entry.getKey())
                        .requests(entry.getValue())
                        .build())
                .toList();
    }


    public static List<FeatureStatistics> emptyFeatureStatistics() {

        return Arrays.stream(FeatureName.values())
                .map(FeatureStatistics::empty)
                .toList();
    }
}
