package com.healthcare.user_service.outbox.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
public class OutboxMetricsService {

    private final Counter publishSuccessCounter;
    private final Counter publishFailureCounter;
    private final Counter publishRetryCounter;
    private final Counter recoveredProcessingCounter;
    private final Counter cleanupDeletedCounter;
    private final Timer publishDurationTimer;

    public OutboxMetricsService(MeterRegistry meterRegistry) {
        this.publishSuccessCounter = Counter.builder("outbox.publish.success")
                .description("Successfully published outbox events")
                .register(meterRegistry);

        this.publishFailureCounter = Counter.builder("outbox.publish.failure")
                .description("Failed outbox publish attempts")
                .register(meterRegistry);

        this.publishRetryCounter = Counter.builder("outbox.publish.retry")
                .description("Outbox events returned to NEW for retry")
                .register(meterRegistry);

        this.recoveredProcessingCounter = Counter.builder("outbox.processing.recovered")
                .description("Stuck PROCESSING outbox events recovered to NEW")
                .register(meterRegistry);

        this.cleanupDeletedCounter = Counter.builder("outbox.cleanup.deleted")
                .description("Deleted old published outbox events")
                .register(meterRegistry);

        this.publishDurationTimer = Timer.builder("outbox.publish.duration")
                .description("Outbox publish duration")
                .register(meterRegistry);
    }

    public void incrementPublishSuccess() {
        publishSuccessCounter.increment();
    }

    public void incrementPublishFailure() {
        publishFailureCounter.increment();
    }

    public void incrementPublishRetry() {
        publishRetryCounter.increment();
    }

    public void incrementRecoveredProcessing(long count) {
        recoveredProcessingCounter.increment(count);
    }

    public void incrementCleanupDeleted(long count) {
        cleanupDeletedCounter.increment(count);
    }

    public <T> T recordPublishDuration(Supplier<T> supplier) {
        return publishDurationTimer.record(supplier);
    }

    public void recordPublishDuration(Runnable runnable) {
        publishDurationTimer.record(runnable);
    }
}
