package com.healthcare.user_service.outbox.cleanup;

import com.healthcare.user_service.config.AbstractMySqlTestContainer;
import com.healthcare.user_service.outbox.constant.OutboxStatus;
import com.healthcare.user_service.outbox.model.OutboxEvent;
import com.healthcare.user_service.outbox.repository.OutboxEventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("it")
@TestPropertySource(properties = {
        "user-context-filter.enabled=false",
        "spring.task.scheduling.enabled=false",
        "app.outbox.cleanup.cron=0 0 3 * * *",
        "app.outbox.cleanup.retention-days=7"
})
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Outbox cleanup integration test")
class OutboxCleanupJobIntegrationTest extends AbstractMySqlTestContainer {

    @Autowired
    private OutboxCleanupJob outboxCleanupJob;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Test
    void should_delete_only_old_published_outbox_events() {

        OutboxEvent oldPublished = outboxEventRepository.save(
                newOutboxEvent(
                        OutboxStatus.PUBLISHED,
                        Instant.now().minus(10, ChronoUnit.DAYS)
                )
        );

        OutboxEvent recentPublished = outboxEventRepository.save(
                newOutboxEvent(
                        OutboxStatus.PUBLISHED,
                        Instant.now()
                )
        );

        OutboxEvent oldFailed = outboxEventRepository.save(
                newOutboxEvent(
                        OutboxStatus.FAILED,
                        Instant.now().minus(10, ChronoUnit.DAYS)
                )
        );

        outboxCleanupJob.cleanupPublishedEvents();

        assertThat(outboxEventRepository.existsById(oldPublished.getId()))
                .isFalse();

        assertThat(outboxEventRepository.existsById(recentPublished.getId()))
                .isTrue();

        assertThat(outboxEventRepository.existsById(oldFailed.getId()))
                .isTrue();
    }

    private OutboxEvent newOutboxEvent(
            OutboxStatus status,
            Instant publishedAt
    ) {
        UUID eventId = UUID.randomUUID();

        return OutboxEvent.builder()
                .eventId(eventId)
                .aggregateType("USER")
                .aggregateId("100")
                .topic("user.registered.v1")
                .eventType("USER_REGISTERED")
                .payload("""
                        {
                          "eventId": "%s",
                          "eventType": "USER_REGISTERED",
                          "occurredAt": "%s",
                          "userId": 100,
                          "email": "cleanup-test@example.com"
                        }
                        """.formatted(eventId, Instant.now()))
                .occurredAt(Instant.now())
                .status(status)
                .attemptCount(1)
                .lastError(null)
                .publishedAt(publishedAt)
                .build();
    }
}
