package com.healthcare.user_service.kafka.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerMetricsService {

    private final Counter processedCounter;
    private final Counter failedCounter;
    private final Counter duplicateCounter;

    public KafkaConsumerMetricsService(MeterRegistry meterRegistry) {
        this.processedCounter = Counter.builder("kafka.consumer.processed")
                .description("Successfully processed Kafka events")
                .register(meterRegistry);

        this.failedCounter = Counter.builder("kafka.consumer.failed")
                .description("Failed Kafka consumer processing")
                .register(meterRegistry);

        this.duplicateCounter = Counter.builder("kafka.consumer.duplicate")
                .description("Duplicate Kafka events skipped")
                .register(meterRegistry);
    }

    public void incrementProcessed() {
        processedCounter.increment();
    }

    public void incrementFailed() {
        failedCounter.increment();
    }

    public void incrementDuplicate() {
        duplicateCounter.increment();
    }
}
