package com.healthcare.user_service.outbox.metrics;

import com.healthcare.user_service.outbox.constant.OutboxStatus;
import com.healthcare.user_service.outbox.repository.OutboxEventRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class OutboxGaugeMetrics {

    public OutboxGaugeMetrics(
            MeterRegistry meterRegistry,
            OutboxEventRepository repository
    ) {
        Gauge.builder("outbox.events.new", repository,
                        repo -> repo.countByStatus(OutboxStatus.NEW))
                .description("Number of NEW outbox events")
                .register(meterRegistry);

        Gauge.builder("outbox.events.processing", repository,
                        repo -> repo.countByStatus(OutboxStatus.PROCESSING))
                .description("Number of PROCESSING outbox events")
                .register(meterRegistry);

        Gauge.builder("outbox.events.failed", repository,
                        repo -> repo.countByStatus(OutboxStatus.FAILED))
                .description("Number of FAILED outbox events")
                .register(meterRegistry);
    }
}
