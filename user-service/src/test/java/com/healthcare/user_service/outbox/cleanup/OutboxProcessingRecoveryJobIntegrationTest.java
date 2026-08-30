package com.healthcare.user_service.outbox.cleanup;

import com.healthcare.user_service.config.AbstractKafkaRedisMsqlTestContainer;
import com.healthcare.user_service.outbox.constant.OutboxStatus;
import com.healthcare.user_service.outbox.model.OutboxEvent;
import com.healthcare.user_service.outbox.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.fail-fast=false",

        "spring.cloud.discovery.enabled=false",

        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",

        "spring.kafka.listener.auto-startup=false",

        "user-context-filter.enabled=false",

        "spring.task.scheduling.enabled=false",
        "app.outbox.publisher.enabled=false",

        "app.outbox.recovery.cron=0 0 0 1 1 *",
        "app.outbox.recovery.timeout-minutes=15"
})
@ActiveProfiles("it")
class OutboxProcessingRecoveryJobIntegrationTest
        extends AbstractKafkaRedisMsqlTestContainer {

    @Autowired
    private OutboxProcessingRecoveryJob recoveryJob;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @BeforeEach
    void cleanDatabase() {
        outboxEventRepository.deleteAll();
    }

    @Test
    void should_recover_only_stuck_processing_events() {
        OutboxEvent oldProcessing = outboxEventRepository.save(
                newOutboxEvent(OutboxStatus.PROCESSING)
        );

        OutboxEvent recentProcessing = outboxEventRepository.save(
                newOutboxEvent(OutboxStatus.PROCESSING)
        );

        OutboxEvent published = outboxEventRepository.save(
                newOutboxEvent(OutboxStatus.PUBLISHED)
        );

        OutboxEvent failed = outboxEventRepository.save(
                newOutboxEvent(OutboxStatus.FAILED)
        );

        outboxEventRepository.updateCreatedAtById(
                oldProcessing.getId(),
                Instant.now().minus(30, ChronoUnit.MINUTES)
        );

        outboxEventRepository.updateCreatedAtById(
                recentProcessing.getId(),
                Instant.now().minus(5, ChronoUnit.MINUTES)
        );

        outboxEventRepository.updateCreatedAtById(
                published.getId(),
                Instant.now().minus(30, ChronoUnit.MINUTES)
        );

        outboxEventRepository.updateCreatedAtById(
                failed.getId(),
                Instant.now().minus(30, ChronoUnit.MINUTES)
        );

        recoveryJob.recoverStuckProcessingEvents();

        OutboxEvent recoveredOldProcessing = outboxEventRepository
                .findById(oldProcessing.getId())
                .orElseThrow();

        OutboxEvent untouchedRecentProcessing = outboxEventRepository
                .findById(recentProcessing.getId())
                .orElseThrow();

        OutboxEvent untouchedPublished = outboxEventRepository
                .findById(published.getId())
                .orElseThrow();

        OutboxEvent untouchedFailed = outboxEventRepository
                .findById(failed.getId())
                .orElseThrow();

        assertThat(recoveredOldProcessing.getStatus())
                .isEqualTo(OutboxStatus.NEW);

        assertThat(recoveredOldProcessing.getLastError())
                .isEqualTo("PROCESSING_TIMEOUT_RECOVERED");

        assertThat(untouchedRecentProcessing.getStatus())
                .isEqualTo(OutboxStatus.PROCESSING);

        assertThat(untouchedPublished.getStatus())
                .isEqualTo(OutboxStatus.PUBLISHED);

        assertThat(untouchedFailed.getStatus())
                .isEqualTo(OutboxStatus.FAILED);
    }

    private OutboxEvent newOutboxEvent(OutboxStatus status) {
        UUID eventId = UUID.randomUUID();

        return OutboxEvent.builder()
                .eventId(eventId)
                .aggregateType("USER")
                .aggregateId("100")
                .topic("user.registered.v1")
                .eventType("USER_REGISTERED")
                .payload("{}")
                .occurredAt(Instant.now())
                .status(status)
                .attemptCount(1)
                .publishedAt(status == OutboxStatus.PUBLISHED ? Instant.now() : null)
                .build();
    }
}
