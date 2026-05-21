package com.healthcare.user_service.outbox.publisher;

import com.healthcare.user_service.config.AbstractMySqlTestContainer;

import com.healthcare.user_service.kafka.producer.interfaces.KafkaEventSender;
import com.healthcare.user_service.outbox.constant.OutboxStatus;
import com.healthcare.user_service.outbox.model.OutboxEvent;
import com.healthcare.user_service.outbox.repository.OutboxEventRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static com.healthcare.user_service.outbox.constant.OutboxConstant.MAX_ATTEMPTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("it")
@TestPropertySource(properties = {
        "spring.task.scheduling.enabled=false",
        "user-context-filter.enabled=false"
})
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Outbox publishing service test")
class OutboxPublishingServiceTest extends AbstractMySqlTestContainer {

    @Autowired
    private OutboxPublishingService publishingService;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @MockitoBean
    private KafkaEventSender kafkaEventSender;

    @MockitoBean
    private OutboxPublisher outboxPublisher;

    @BeforeEach
    void cleanDatabase() {
        outboxEventRepository.deleteAll();
        reset(kafkaEventSender);
    }

    @Test
    void should_claim_new_events_and_mark_them_as_processing() {
        OutboxEvent event = outboxEventRepository.save(
                newOutboxEvent(OutboxStatus.NEW, 0)
        );

        List<Long> claimedIds = publishingService.claimBatch();

        OutboxEvent updatedEvent = findEvent(event.getId());

        assertThat(claimedIds)
                .containsExactly(event.getId());

        assertThat(updatedEvent.getStatus())
                .isEqualTo(OutboxStatus.PROCESSING);
    }

    @Test
    void should_publish_processing_event() {
        OutboxEvent event = outboxEventRepository.save(
                newOutboxEvent(OutboxStatus.PROCESSING, 0)
        );

        doNothing()
                .when(kafkaEventSender)
                .send(any(OutboxEvent.class));

        publishingService.publishSingle(event.getId());

        OutboxEvent updatedEvent = findEvent(event.getId());

        assertThat(updatedEvent.getStatus())
                .isEqualTo(OutboxStatus.PUBLISHED);

        assertThat(updatedEvent.getPublishedAt())
                .isNotNull();

        assertThat(updatedEvent.getAttemptCount())
                .isEqualTo(1);

        assertThat(updatedEvent.getLastError())
                .isNull();

        verify(kafkaEventSender, times(1))
                .send(any(OutboxEvent.class));
    }

    @Test
    void should_return_event_to_new_when_kafka_publish_fails() {
        OutboxEvent event = outboxEventRepository.save(
                newOutboxEvent(OutboxStatus.PROCESSING, 0)
        );

        doThrow(new RuntimeException("Kafka is down"))
                .when(kafkaEventSender)
                .send(any(OutboxEvent.class));

        publishingService.publishSingle(event.getId());

        OutboxEvent updatedEvent = findEvent(event.getId());

        assertThat(updatedEvent.getStatus())
                .isEqualTo(OutboxStatus.NEW);

        assertThat(updatedEvent.getAttemptCount())
                .isEqualTo(1);

        assertThat(updatedEvent.getLastError())
                .contains("Kafka is down");

        assertThat(updatedEvent.getPublishedAt())
                .isNull();

        verify(kafkaEventSender, times(1))
                .send(any(OutboxEvent.class));
    }

    @Test
    void should_mark_event_as_failed_when_max_attempts_exceeded() {
        OutboxEvent event = outboxEventRepository.save(
                newOutboxEvent(OutboxStatus.PROCESSING, MAX_ATTEMPTS)
        );

        publishingService.publishSingle(event.getId());

        OutboxEvent updatedEvent = findEvent(event.getId());

        assertThat(updatedEvent.getStatus())
                .isEqualTo(OutboxStatus.FAILED);

        assertThat(updatedEvent.getLastError())
                .isEqualTo("MAX_ATTEMPTS_EXCEEDED");

        assertThat(updatedEvent.getPublishedAt())
                .isNull();

        verifyNoInteractions(kafkaEventSender);
    }

    @Test
    void should_claim_only_new_events() {
        OutboxEvent newEvent = outboxEventRepository.save(
                newOutboxEvent(OutboxStatus.NEW, 0)
        );

        outboxEventRepository.save(
                newOutboxEvent(OutboxStatus.PROCESSING, 0)
        );

        outboxEventRepository.save(
                newOutboxEvent(OutboxStatus.PUBLISHED, 1)
        );

        outboxEventRepository.save(
                newOutboxEvent(OutboxStatus.FAILED, MAX_ATTEMPTS)
        );

        List<Long> claimedIds = publishingService.claimBatch();

        assertThat(claimedIds)
                .containsExactly(newEvent.getId());
    }

    @Test
    void should_publish_event_only_once_when_two_publishers_run_concurrently() throws Exception {
        OutboxEvent event = outboxEventRepository.save(
                newOutboxEvent(OutboxStatus.NEW, 0)
        );

        doNothing()
                .when(kafkaEventSender)
                .send(any(OutboxEvent.class));

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        Callable<Void> publisherTask = () -> {
            startLatch.await();

            List<Long> claimedIds = publishingService.claimBatch();

            for (Long claimedId : claimedIds) {
                publishingService.publishSingle(claimedId);
            }

            return null;
        };

        Future<Void> first = executorService.submit(publisherTask);
        Future<Void> second = executorService.submit(publisherTask);

        startLatch.countDown();

        first.get(10, TimeUnit.SECONDS);
        second.get(10, TimeUnit.SECONDS);

        executorService.shutdown();

        OutboxEvent updatedEvent = findEvent(event.getId());

        assertThat(updatedEvent.getStatus())
                .isEqualTo(OutboxStatus.PUBLISHED);

        assertThat(updatedEvent.getAttemptCount())
                .isEqualTo(1);

        assertThat(updatedEvent.getPublishedAt())
                .isNotNull();

        verify(kafkaEventSender, times(1))
                .send(any(OutboxEvent.class));
    }

    private OutboxEvent findEvent(Long id) {
        return outboxEventRepository.findById(id)
                .orElseThrow();
    }

    private OutboxEvent newOutboxEvent(
            OutboxStatus status,
            int attemptCount
    ) {
        return OutboxEvent.builder()
                .eventId(UUID.randomUUID())
                .aggregateType("USER")
                .aggregateId("100")
                .eventType("USER_REGISTERED")
                .payload("""
                        {
                          "userId": 100,
                          "email": "test@example.com"
                        }
                        """)
                .topic("user.registered.v1")
                .status(status)
                .attemptCount(attemptCount)
                .occurredAt(Instant.now())
                .build();
    }
}