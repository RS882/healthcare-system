package com.healthcare.aiservice.model;


import com.healthcare.aiservice.common.provider.logging.AiRequestStatus;
import com.healthcare.aiservice.config.constant.FeatureName;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Builder
@Document(collection = "ai_request_logs")
public record AiRequestLog(

        @Id
        String id,

        FeatureName feature,

        String provider,

        String model,

        Object request,

        Object response,

        AiRequestStatus status,

        Long durationMs,

        String errorType,

        String errorMessage,

        Instant createdAt
) {
}
