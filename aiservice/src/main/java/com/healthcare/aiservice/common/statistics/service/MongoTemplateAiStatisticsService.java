package com.healthcare.aiservice.common.statistics.service;


import com.healthcare.aiservice.common.provider.logging.AiRequestStatus;
import com.healthcare.aiservice.common.statistics.dto.AiStatisticsMapper;
import com.healthcare.aiservice.common.statistics.dto.AiStatisticsResponse;
import com.healthcare.aiservice.common.statistics.dto.FeatureStatistics;
import com.healthcare.aiservice.common.statistics.service.interfaces.AiStatisticService;
import com.healthcare.aiservice.config.constant.FeatureName;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.FacetOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
@RequiredArgsConstructor
public class MongoTemplateAiStatisticsService implements AiStatisticService {

    private static final String COLLECTION_NAME = "ai_request_logs";

    private static final String FIELD_STATUS = "status";
    private static final String FIELD_FEATURE = "feature";
    private static final String FIELD_DURATION_MS = "durationMs";

    private static final String FACET_TOTAL = "total";
    private static final String FACET_SUCCESSFUL = "successful";
    private static final String FACET_FAILED = "failed";
    private static final String FACET_AVERAGE = "average";
    private static final String FACET_BY_FEATURE = "byFeature";

    private static final String KEY_COUNT = "count";
    private static final String KEY_ID = "_id";
    private static final String KEY_AVERAGE_DURATION_MS = "averageDurationMs";

    private final MongoTemplate mongoTemplate;

    @Override
    public AiStatisticsResponse getStatistic() {
        Aggregation aggregation = Aggregation.newAggregation(buildStatisticsFacet());

        Document result = mongoTemplate.aggregate(
                aggregation,
                COLLECTION_NAME,
                Document.class
        ).getUniqueMappedResult();

        return result == null
                ? AiStatisticsResponse.empty()
                : mapToResponse(result);
    }

    private FacetOperation buildStatisticsFacet() {
        return facet(count().as(KEY_COUNT)).as(FACET_TOTAL)
                .and(
                        match(Criteria.where(FIELD_STATUS).is(AiRequestStatus.SUCCESS)),
                        count().as(KEY_COUNT)
                ).as(FACET_SUCCESSFUL)
                .and(
                        match(Criteria.where(FIELD_STATUS).is(AiRequestStatus.FAILED)),
                        count().as(KEY_COUNT)
                ).as(FACET_FAILED)
                .and(
                        group().avg(FIELD_DURATION_MS).as(KEY_AVERAGE_DURATION_MS)
                ).as(FACET_AVERAGE)
                .and(
                        group(FIELD_FEATURE).count().as(KEY_COUNT)
                ).as(FACET_BY_FEATURE);
    }

    private AiStatisticsResponse mapToResponse(Document result) {
        return AiStatisticsResponse.builder()
                .totalRequests(readCountFromFacet(result, FACET_TOTAL))
                .successfulRequests(readCountFromFacet(result, FACET_SUCCESSFUL))
                .failedRequests(readCountFromFacet(result, FACET_FAILED))
                .averageDurationMs(readAverageDuration(result))
                .requestsByFeature(readRequestsByFeature(result))
                .build();
    }

    private long readCountFromFacet(Document result, String facetName) {
        return resolveFirstDocumentFromFacet(result, facetName)
                .map(document -> document.get(KEY_COUNT, Number.class))
                .map(Number::longValue)
                .orElse(0L);
    }

    private long readAverageDuration(Document result) {
        return resolveFirstDocumentFromFacet(result, FACET_AVERAGE)
                .map(document -> document.get(KEY_AVERAGE_DURATION_MS, Number.class))
                .map(Number::doubleValue)
                .map(Math::round)
                .orElse(0L);
    }

    private List<FeatureStatistics> readRequestsByFeature(Document result) {
        List<Document> documents = resolveDocumentsFromFacet(result, FACET_BY_FEATURE);

        if (documents.isEmpty()) {
            return AiStatisticsMapper.emptyFeatureStatistics();
        }

        Map<FeatureName, Long> statistics =
                AiStatisticsMapper.initializeFeatureStatisticsMap();

        for (Document document : documents) {
            applyFeatureCount(statistics, document);
        }

        return AiStatisticsMapper.convertFeatureStatisticsMapToFeatureStatisticsList(statistics);
    }

    private void applyFeatureCount(
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

    private Optional<Document> resolveFirstDocumentFromFacet(
            Document document,
            String facetName
    ) {
        List<Document> documents = resolveDocumentsFromFacet(document, facetName);

        return documents.isEmpty()
                ? Optional.empty()
                : Optional.of(documents.get(0));
    }

    private List<Document> resolveDocumentsFromFacet(Document document, String facetName) {
        if (document == null) {
            return List.of();
        }

        List<Document> documents = document.getList(facetName, Document.class);

        return documents == null ? List.of() : documents;
    }
}