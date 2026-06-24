package com.healthcare.aiservice.repository;


import com.healthcare.aiservice.common.provider.logging.AiRequestStatus;
import com.healthcare.aiservice.config.constant.FeatureName;
import com.healthcare.aiservice.common.provider.logging.model.AiRequestLog;
import com.healthcare.aiservice.repository.projection.AverageDurationProjection;
import com.healthcare.aiservice.repository.projection.FeatureCountProjection;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AiRequestLogRepository extends MongoRepository<AiRequestLog, String> {

    long countByStatus(AiRequestStatus status);

    long countByFeature(FeatureName feature);

    long countByFeatureAndStatus(FeatureName feature, AiRequestStatus status);

    @Aggregation(pipeline = {
            "{ '$group': { '_id': '$feature', 'count': { '$sum': 1 } } }",
            "{ '$project': { '_id': 0, 'feature': '$_id', 'count': 1 } }"
    })
    List<FeatureCountProjection> countRequestsGroupedByFeature();

    @Aggregation(pipeline = {
            "{ '$group': { '_id': null, 'averageDurationMs': { '$avg': '$durationMs' } } }"
    })
    AverageDurationProjection findAverageDuration();
}