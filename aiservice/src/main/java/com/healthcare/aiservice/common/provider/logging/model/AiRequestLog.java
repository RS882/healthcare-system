package com.healthcare.aiservice.common.provider.logging.model;


import com.healthcare.aiservice.common.provider.logging.AiRequestStatus;
import com.healthcare.aiservice.config.constant.FeatureName;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Builder
@Document(collection = "ai_request_logs")

@CompoundIndex(
        name = "feature_status_created_idx",
        def = "{'feature': 1, 'status': 1, 'createdAt': -1}"
)
public record AiRequestLog(

        @Id
        String id,

        FeatureName feature,

        String provider,

        String model,

        String request,

        String response,

        AiRequestStatus status,

        Long durationMs,

        String errorType,

        String errorMessage,

        @Indexed(expireAfter = "30d")
        Instant createdAt
) {
}
