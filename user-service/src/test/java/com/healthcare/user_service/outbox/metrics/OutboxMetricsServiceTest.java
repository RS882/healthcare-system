package com.healthcare.user_service.outbox.metrics;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Outbox metrics service test")
class OutboxMetricsServiceTest {

    private SimpleMeterRegistry meterRegistry;
    private OutboxMetricsService metricsService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metricsService = new OutboxMetricsService(meterRegistry);
    }

    @Test
    void should_increment_publish_success_counter() {
        metricsService.incrementPublishSuccess();

        double count = meterRegistry
                .counter("outbox.publish.success")
                .count();

        assertThat(count).isEqualTo(1.0);
    }

    @Test
    void should_increment_publish_failure_counter() {
        metricsService.incrementPublishFailure();

        double count = meterRegistry
                .counter("outbox.publish.failure")
                .count();

        assertThat(count).isEqualTo(1.0);
    }

    @Test
    void should_increment_publish_retry_counter() {
        metricsService.incrementPublishRetry();

        double count = meterRegistry
                .counter("outbox.publish.retry")
                .count();

        assertThat(count).isEqualTo(1.0);
    }

    @Test
    void should_increment_processing_recovered_counter_by_count() {
        metricsService.incrementRecoveredProcessing(3);

        double count = meterRegistry
                .counter("outbox.processing.recovered")
                .count();

        assertThat(count).isEqualTo(3.0);
    }

    @Test
    void should_increment_cleanup_deleted_counter_by_count() {
        metricsService.incrementCleanupDeleted(5);

        double count = meterRegistry
                .counter("outbox.cleanup.deleted")
                .count();

        assertThat(count).isEqualTo(5.0);
    }

    @Test
    void should_record_publish_duration() {
        metricsService.recordPublishDuration(() -> {
            // simulate publish operation
        });

        long count = meterRegistry
                .timer("outbox.publish.duration")
                .count();

        assertThat(count).isEqualTo(1);
    }
}
