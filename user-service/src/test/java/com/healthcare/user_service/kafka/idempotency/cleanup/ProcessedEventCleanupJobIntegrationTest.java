package com.healthcare.user_service.kafka.idempotency.cleanup;

import com.healthcare.user_service.config.AbstractMySqlTestContainer;
import com.healthcare.user_service.kafka.idempotency.model.ProcessedEvent;
import com.healthcare.user_service.kafka.idempotency.model.ProcessedEventId;
import com.healthcare.user_service.kafka.idempotency.repository.ProcessedEventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("it")
@TestPropertySource(properties = {
        "user-context-filter.enabled=false",
        "spring.task.scheduling.enabled=false",
        "app.processed-event.cleanup.cron=0 30 3 * * *",
        "app.processed-event.cleanup.retention-days=30"
})
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Processed event cleanup integration test")
class ProcessedEventCleanupJobIntegrationTest extends AbstractMySqlTestContainer {

    @Autowired
    private ProcessedEventCleanupJob processedEventCleanupJob;

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @Test
    @Transactional
    void should_delete_only_old_processed_events() {
        ProcessedEvent oldProcessed = processedEventRepository.save(
                newProcessedEvent("old-consumer")
        );

        ProcessedEvent recentProcessed = processedEventRepository.save(
                newProcessedEvent("recent-consumer")
        );

        setProcessedAt(oldProcessed.getId(), Instant.now().minus(40, ChronoUnit.DAYS));
        setProcessedAt(recentProcessed.getId(), Instant.now().minus(5, ChronoUnit.DAYS));

        processedEventCleanupJob.cleanupProcessedEvents();

        assertThat(processedEventRepository.existsById(oldProcessed.getId()))
                .isFalse();

        assertThat(processedEventRepository.existsById(recentProcessed.getId()))
                .isTrue();
    }

    private ProcessedEvent newProcessedEvent(String consumerName) {
        return ProcessedEvent.builder()
                .id(new ProcessedEventId(
                        UUID.randomUUID(),
                        consumerName
                ))
                .build();
    }

    private void setProcessedAt(ProcessedEventId id, Instant processedAt) {
        processedEventRepository.updateProcessedAtById(
                id.getEventId(),
                id.getConsumerName(),
                processedAt
        );
    }
}
