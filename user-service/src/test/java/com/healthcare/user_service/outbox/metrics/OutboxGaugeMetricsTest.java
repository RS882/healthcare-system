package com.healthcare.user_service.outbox.metrics;

import com.healthcare.user_service.outbox.constant.OutboxStatus;
import com.healthcare.user_service.outbox.repository.OutboxEventRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Outbox gauge metrics test")
class OutboxGaugeMetricsTest {

    @Test
    void should_register_outbox_status_gauges() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

        OutboxEventRepository repository = mock(OutboxEventRepository.class);

        when(repository.countByStatus(OutboxStatus.NEW)).thenReturn(3L);
        when(repository.countByStatus(OutboxStatus.PROCESSING)).thenReturn(2L);
        when(repository.countByStatus(OutboxStatus.FAILED)).thenReturn(1L);

        new OutboxGaugeMetrics(meterRegistry, repository);

        assertThat(meterRegistry.get("outbox.events.new").gauge().value())
                .isEqualTo(3.0);

        assertThat(meterRegistry.get("outbox.events.processing").gauge().value())
                .isEqualTo(2.0);

        assertThat(meterRegistry.get("outbox.events.failed").gauge().value())
                .isEqualTo(1.0);
    }
}
