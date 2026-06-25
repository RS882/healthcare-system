package com.healthcare.aiservice.repository;

import com.healthcare.aiservice.common.provider.logging.AiRequestStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.FacetOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;
import org.bson.Document;

import static com.healthcare.aiservice.common.statistics.constnats.AiStatisticsAggregationConstants.*;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.count;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;

@Repository
@RequiredArgsConstructor
public class MongoTemplateAiStatisticsRepository implements AiStatisticsRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public Document getAiStatistics() {
        Aggregation aggregation = Aggregation.newAggregation(buildStatisticsFacet());

        return mongoTemplate.aggregate(
                aggregation,
                COLLECTION_NAME,
                Document.class
        ).getUniqueMappedResult();
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
                        context -> new Document("$group",
                                new Document(KEY_ID, "$" + FIELD_FEATURE)
                                        .append(KEY_COUNT, new Document("$sum", 1))
                        )
                ).as(FACET_BY_FEATURE);
    }
}
