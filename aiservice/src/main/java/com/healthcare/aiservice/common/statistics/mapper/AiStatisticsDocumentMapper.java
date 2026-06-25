package com.healthcare.aiservice.common.statistics.mapper;

import com.healthcare.aiservice.common.statistics.dto.AiStatisticsResponse;
import com.healthcare.aiservice.common.statistics.dto.FeatureStatistics;
import com.healthcare.aiservice.config.constant.FeatureName;
import org.bson.Document;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.healthcare.aiservice.common.statistics.constnats.AiStatisticsAggregationConstants.*;
import static com.healthcare.aiservice.common.statistics.constnats.AiStatisticsAggregationConstants.FACET_AVERAGE;
import static com.healthcare.aiservice.common.statistics.constnats.AiStatisticsAggregationConstants.FACET_BY_FEATURE;
import static com.healthcare.aiservice.common.statistics.constnats.AiStatisticsAggregationConstants.KEY_AVERAGE_DURATION_MS;
import static com.healthcare.aiservice.common.statistics.constnats.AiStatisticsAggregationConstants.KEY_COUNT;
import static com.healthcare.aiservice.common.statistics.constnats.AiStatisticsAggregationConstants.KEY_ID;

public final class AiStatisticsDocumentMapper {
    private AiStatisticsDocumentMapper() {}

    public static AiStatisticsResponse mapToResponse(Document result) {
        return AiStatisticsResponse.builder()
                .totalRequests(readCountFromFacet(result, FACET_TOTAL))
                .successfulRequests(readCountFromFacet(result, FACET_SUCCESSFUL))
                .failedRequests(readCountFromFacet(result, FACET_FAILED))
                .averageDurationMs(readAverageDuration(result))
                .requestsByFeature(readRequestsByFeature(result))
                .build();
    }

    private static long readCountFromFacet(Document result, String facetName) {
        return resolveFirstDocumentFromFacet(result, facetName)
                .map(document -> document.get(KEY_COUNT, Number.class))
                .map(Number::longValue)
                .orElse(0L);
    }

    private static long readAverageDuration(Document result) {
        return resolveFirstDocumentFromFacet(result, FACET_AVERAGE)
                .map(document -> document.get(KEY_AVERAGE_DURATION_MS, Number.class))
                .map(Number::doubleValue)
                .map(Math::round)
                .orElse(0L);
    }

    private  static List<FeatureStatistics> readRequestsByFeature(Document result) {
        List<Document> documents = resolveDocumentsFromFacet(result, FACET_BY_FEATURE);

        if (documents.isEmpty()) {
            return FeatureStatisticsMapper.empty();
        }

        Map<FeatureName, Long> statistics =
                FeatureStatisticsMapper.initializeFeatureStatisticsMap();

        for (Document document : documents) {
            applyFeatureCount(statistics, document);
        }

        return FeatureStatisticsMapper.toFeatureStatisticsList(statistics);
    }

    private static void applyFeatureCount(
            Map<FeatureName, Long> statistics,
            Document document
    ) {
        String featureName = document.getString(KEY_ID);
        Number count = document.get(KEY_COUNT, Number.class);

        if (featureName == null || count == null) {
            return;
        }

        try {
            FeatureName feature = FeatureName.valueOf(featureName);
            statistics.put(feature, count.longValue());
        } catch (IllegalArgumentException ignored) {
            // Ignore unknown feature names stored in MongoDB
        }
    }

    private static Optional<Document> resolveFirstDocumentFromFacet(
            Document document,
            String facetName
    ) {
        List<Document> documents = resolveDocumentsFromFacet(document, facetName);

        return documents.isEmpty()
                ? Optional.empty()
                : Optional.of(documents.get(0));
    }

    private static List<Document> resolveDocumentsFromFacet(Document document, String facetName) {
        if (document == null) {
            return List.of();
        }

        List<Document> documents = document.getList(facetName, Document.class);

        return documents == null ? List.of() : documents;
    }
}
