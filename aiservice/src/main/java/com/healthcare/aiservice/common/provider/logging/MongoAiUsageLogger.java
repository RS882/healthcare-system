package com.healthcare.aiservice.common.provider.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.aiservice.config.constant.FeatureName;

import com.healthcare.aiservice.common.provider.logging.model.AiRequestLog;
import com.healthcare.aiservice.repository.AiRequestLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class MongoAiUsageLogger implements AiUsageLogger {

    private static final int MAX_LOG_LENGTH = 10000;

    private final AiRequestLogRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    public void logSuccess(
            FeatureName feature,
            String provider,
            String model,
            Object request,
            Object response,
            long durationMs
    ) {
        repository.save(AiRequestLog.builder()
                .feature(feature)
                .provider(provider)
                .model(model)
                .request(truncate(toJson(request)))
                .response(truncate(toJson(response)))
                .status(AiRequestStatus.SUCCESS)
                .durationMs(durationMs)
                .createdAt(Instant.now())
                .build());
    }

    @Override
    public void logFailure(
            FeatureName feature,
            String provider,
            String model,
            Object request,
            Throwable exception,
            long durationMs
    ) {
        repository.save(AiRequestLog.builder()
                .feature(feature)
                .provider(provider)
                .model(model)
                .request(truncate(toJson(request)))
                .status(AiRequestStatus.FAILED)
                .errorType(exception.getClass().getSimpleName())
                .errorMessage(exception.getMessage())
                .durationMs(durationMs)
                .createdAt(Instant.now())
                .build());
    }

    private String toJson(Object value) {

        if (value == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return String.valueOf(value);
        }
    }

    private String truncate(String value) {

        if (value == null) {
            return null;
        }

        return value.length() <= MAX_LOG_LENGTH
                ? value
                : value.substring(0, MAX_LOG_LENGTH);
    }
}
