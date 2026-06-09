package com.healthcare.aiservice.common.provider.logging;


import com.healthcare.aiservice.config.constant.FeatureName;

public interface AiUsageLogger {

    void logSuccess(
            FeatureName feature,
            String provider,
            String model,
            Object request,
            Object response,
            long durationMs
    );

    void logFailure(
            FeatureName feature,
            String provider,
            String model,
            Object request,
            Throwable exception,
            long durationMs
    );
}
