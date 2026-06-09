package com.healthcare.aiservice.common.provider.logging;

import com.healthcare.aiservice.config.constant.FeatureName;

import com.healthcare.aiservice.model.AiRequestLog;
import com.healthcare.aiservice.repository.AiRequestLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class MongoAiUsageLogger implements AiUsageLogger {

    private final AiRequestLogRepository repository;

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
                .request(request)
                .response(response)
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
                .request(request)
                .status(AiRequestStatus.FAILED)
                .errorType(exception.getClass().getSimpleName())
                .errorMessage(exception.getMessage())
                .durationMs(durationMs)
                .createdAt(Instant.now())
                .build());
    }
}
