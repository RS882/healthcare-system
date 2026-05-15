package com.healthcare.user_service.outbox;

import com.healthcare.user_service.outbox.constant.OutboxStatus;
import com.healthcare.user_service.outbox.model.OutboxEvent;
import com.healthcare.user_service.outbox.publisher.OutboxPublisher;
import com.healthcare.user_service.outbox.repository.OutboxEventRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.healthcare.user_service.outbox.constant.OutboxConstant.MAX_ATTEMPTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@SpringBootTest(properties = {
        "spring.task.scheduling.enabled=false"
})
@ActiveProfiles("it")
@TestPropertySource(properties = {
        "user-context-filter.enabled=false"
})
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Outbox publisher test")
class OutboxPublisherTest extends AbstractMySqlTestContainer {

    @Autowired
    private OutboxPublisher outboxPublisher;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @MockitoBean(name = "stringKafkaTemplate")
    private KafkaTemplate<String, String> stringKafkaTemplate;

    @BeforeEach
    void cleanDatabase() {
        outboxEventRepository.deleteAll();
        reset(stringKafkaTemplate);
    }

    @Test
    void should_increment_attempt_count_and_keep_status_new_when_kafka_publish_fails() {
        OutboxEvent event = outboxEventRepository.save(newOutboxEvent(0));

        when(stringKafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.failedFuture(
                        new RuntimeException("Kafka is down")
                ));

        outboxPublisher.publish();

        OutboxEvent updated = outboxEventRepository.findById(event.getId())
                .orElseThrow();

        assertThat(updated.getStatus()).isEqualTo(OutboxStatus.NEW);
        assertThat(updated.getAttemptCount()).isEqualTo(1);
        assertThat(updated.getLastError()).contains("Kafka is down");
        assertThat(updated.getPublishedAt()).isNull();

        verify(stringKafkaTemplate, times(1))
                .send(event.getTopic(), event.getAggregateId(), event.getPayload());
    }

    @Test
    void should_mark_event_as_failed_when_max_attempts_exceeded() {
        OutboxEvent event = outboxEventRepository.save(newOutboxEvent(MAX_ATTEMPTS));

        outboxPublisher.publish();

        OutboxEvent updated = outboxEventRepository.findById(event.getId())
                .orElseThrow();

        assertThat(updated.getStatus()).isEqualTo(OutboxStatus.FAILED);
        assertThat(updated.getLastError()).isEqualTo("MAX_ATTEMPTS_EXCEEDED");
        assertThat(updated.getPublishedAt()).isNull();

        verifyNoInteractions(stringKafkaTemplate);
    }

    private OutboxEvent newOutboxEvent(int attemptCount) {
        UUID eventId = UUID.randomUUID();
        Instant occurredAt = Instant.now();

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
                          "email": "test@example.com"
                        }
                        """.formatted(eventId, occurredAt))
                .occurredAt(occurredAt)
                .status(OutboxStatus.NEW)
                .attemptCount(attemptCount)
                .lastError(null)
                .publishedAt(null)
                .build();
    }
}
