package com.healthcare.user_service.kafka.idempotency.cleanup;

import com.healthcare.user_service.kafka.idempotency.repository.ProcessedEventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessedEventCleanupJob {

    private final ProcessedEventRepository repository;
    private final ProcessedEventCleanupProperties properties;

    @Scheduled(cron = "${app.processed-event.cleanup.cron:0 30 3 * * *}")
    @Transactional
    public void cleanupProcessedEvents() {
        Instant threshold = Instant.now()
                .minus(properties.retentionDays(), ChronoUnit.DAYS);

        long deletedCount = repository.deleteByProcessedAtBefore(threshold);

        log.info(
                "Processed event cleanup completed: deleted {} events older than {} days",
                deletedCount,
                properties.retentionDays()
        );
    }
}
