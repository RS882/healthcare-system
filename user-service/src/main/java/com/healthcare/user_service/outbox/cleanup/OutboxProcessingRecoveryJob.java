package com.healthcare.user_service.outbox.cleanup;

import com.healthcare.user_service.config.properties.OutboxRecoveryProperties;
import com.healthcare.user_service.outbox.constant.OutboxStatus;
import com.healthcare.user_service.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxProcessingRecoveryJob {

    private final OutboxEventRepository repository;
    private final OutboxRecoveryProperties properties;

    @Scheduled(cron = "${app.outbox.recovery.cron}")
    @Transactional
    public void recoverStuckProcessingEvents() {
        Instant threshold = Instant.now()
                .minus(properties.timeoutMinutes(), ChronoUnit.MINUTES);

        int  recoveredCount = repository.resetStuckProcessingEvents(
                OutboxStatus.PROCESSING,
                OutboxStatus.NEW,
                threshold,
                "PROCESSING_TIMEOUT_RECOVERED"
        );

        if (recoveredCount > 0) {
            log.warn(
                    "Recovered {} stuck PROCESSING outbox events older than {} minutes",
                    recoveredCount,
                    properties.timeoutMinutes()
            );
        }
    }
}
