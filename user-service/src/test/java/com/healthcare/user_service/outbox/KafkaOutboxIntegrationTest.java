package com.healthcare.user_service.outbox;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.user_service.audit.repository.AuditLogRepository;
import com.healthcare.user_service.config.AbstractKafkaMsqlTestContainer;
import com.healthcare.user_service.kafka.event.UserRegisteredEvent;
import com.healthcare.user_service.kafka.idempotency.model.ProcessedEventId;
import com.healthcare.user_service.kafka.idempotency.repository.ProcessedEventRepository;
import com.healthcare.user_service.kafka.properties.KafkaCustomProperties;
import com.healthcare.user_service.model.dto.request.RegistrationDto;
import com.healthcare.user_service.outbox.constant.OutboxStatus;
import com.healthcare.user_service.outbox.model.OutboxEvent;
import com.healthcare.user_service.outbox.repository.OutboxEventRepository;
import com.healthcare.user_service.service.interfacies.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.healthcare.user_service.kafka.consumer.ConsumerNames.USER_EVENT_CONSUMER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("it")
@TestPropertySource(properties = {
        "user-context-filter.enabled=false",
        "app.kafka.groups.user-service.id=user-service-outbox-test"
})
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Kafka outbox integration test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class KafkaOutboxIntegrationTest extends AbstractKafkaMsqlTestContainer {

    @Autowired
    private UserService userService;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @Autowired
    private KafkaTemplate<String, String> stringKafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaCustomProperties kafkaProperties;

    @BeforeEach
    void cleanDatabase() {
        auditLogRepository.deleteAll();
        processedEventRepository.deleteAll();
        outboxEventRepository.deleteAll();
    }

    @Test
    void should_publish_user_registered_event_and_process() {
        RegistrationDto request = new RegistrationDto(
                "test-" + System.nanoTime() + "@example.com",
                "Test User",
                "Password123!"
        );

        userService.registration(request);

        await()
                .atMost(15, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    List<OutboxEvent> outboxEvents = outboxEventRepository.findAll();

                    assertThat(outboxEvents).hasSize(1);

                    OutboxEvent outboxEvent = outboxEvents.get(0);

                    assertThat(outboxEvent.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
                    assertThat(outboxEvent.getPublishedAt()).isNotNull();
                    assertThat(outboxEvent.getAttemptCount()).isGreaterThanOrEqualTo(1);
                    assertThat(outboxEvent.getLastError()).isNull();

                    ProcessedEventId processedEventId =
                            new ProcessedEventId(outboxEvent.getEventId(), USER_EVENT_CONSUMER);

                    assertThat(auditLogRepository.countByEventId(outboxEvent.getEventId()))
                            .isEqualTo(1);

                    assertThat(processedEventRepository.existsById(processedEventId))
                            .isTrue();
                });
    }
    @Test
    void should_not_create_second_audit_log_for_duplicate_event() throws Exception {
        UserRegisteredEvent event = UserRegisteredEvent.of(
                100L,
                "duplicate@example.com"
        );

        ProcessedEventId processedEventId =
                new ProcessedEventId(event.eventId(), USER_EVENT_CONSUMER);

        String payload = objectMapper.writeValueAsString(event);

        sendInTransaction(
                kafkaProperties.topics().userRegistered().name(),
                String.valueOf(event.userId()),
                payload
        );


        await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    assertThat(auditLogRepository.countByEventId(event.eventId()))
                            .isEqualTo(1);

                    assertThat(processedEventRepository.existsById(processedEventId))
                            .isTrue();
                });

        sendInTransaction(
                kafkaProperties.topics().userRegistered().name(),
                String.valueOf(event.userId()),
                payload
        );

        await()
                .during(3, TimeUnit.SECONDS)
                .atMost(5, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    assertThat(auditLogRepository.countByEventId(event.eventId()))
                            .isEqualTo(1);

                    assertThat(processedEventRepository.existsById(processedEventId))
                            .isTrue();
                });
    }

    private void sendInTransaction(
            String topic,
            String key,
            String payload
    ) {
        stringKafkaTemplate.executeInTransaction(operations -> {
            try {
                operations.send(topic, key, payload)
                        .get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return true;
        });
    }
}