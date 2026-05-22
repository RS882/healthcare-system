package com.healthcare.user_service.kafka.metrics;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Kafka consumer metrics service test")
class KafkaConsumerMetricsServiceTest {

    private SimpleMeterRegistry meterRegistry;
    private KafkaConsumerMetricsService metricsService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metricsService = new KafkaConsumerMetricsService(meterRegistry);
    }

    @Test
    void should_increment_processed_counter() {
        metricsService.incrementProcessed();

        double count = meterRegistry
                .counter("kafka.consumer.processed")
                .count();

        assertThat(count).isEqualTo(1.0);
    }

    @Test
    void should_increment_failed_counter() {
        metricsService.incrementFailed();

        double count = meterRegistry
                .counter("kafka.consumer.failed")
                .count();

        assertThat(count).isEqualTo(1.0);
    }

    @Test
    void should_increment_duplicate_counter() {
        metricsService.incrementDuplicate();

        double count = meterRegistry
                .counter("kafka.consumer.duplicate")
                .count();

        assertThat(count).isEqualTo(1.0);
    }
}
