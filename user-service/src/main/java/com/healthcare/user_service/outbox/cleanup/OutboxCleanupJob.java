package com.healthcare.user_service.outbox.cleanup;


import com.healthcare.user_service.outbox.constant.OutboxStatus;
import com.healthcare.user_service.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.healthcare.user_service.outbox.constant.OutboxConstant.RETENTION_DAYS;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxCleanupJob {

    private final OutboxEventRepository repository;
    private final OutboxCleanupProperties properties;

    @Scheduled(cron = "${app.outbox.cleanup.cron}")
    @Transactional
    public void cleanupPublishedEvents() {
        Instant threshold = Instant.now()
                .minus(properties.retentionDays(), ChronoUnit.DAYS);

        long deletedCount = repository.deleteByStatusAndPublishedAtBefore(
                OutboxStatus.PUBLISHED,
                threshold
        );

        log.info(
                "Outbox cleanup completed: deleted {} published events older than {} days",
                deletedCount,
                properties.retentionDays()
        );
    }
}