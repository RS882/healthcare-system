package com.healthcare.user_service.kafka;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.user_service.audit.service.interfacies.AuditService;

import com.healthcare.user_service.config.AbstractKafkaMsqlTestContainer;
import com.healthcare.user_service.kafka.event.UserRegisteredEvent;
import com.healthcare.user_service.kafka.idempotency.model.ProcessedEventId;
import com.healthcare.user_service.kafka.idempotency.repository.ProcessedEventRepository;
import com.healthcare.user_service.kafka.properties.KafkaCustomProperties;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

import static com.healthcare.user_service.kafka.consumer.ConsumerNames.USER_EVENT_CONSUMER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@ActiveProfiles("it")
@TestPropertySource(properties = {
        "user-context-filter.enabled=false",

        "app.kafka.topics.user-registered.name=user.registered.transaction-test.v1",
        "app.kafka.topics.user-updated.name=user.updated.transaction-test.v1",
        "app.kafka.topics.user-deleted.name=user.deleted.transaction-test.v1",

        "app.kafka.groups.user-service.id=user-service-transaction-test"
})
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Kafka consumer transaction integration test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class KafkaConsumerTransactionIntegrationTest extends AbstractKafkaMsqlTestContainer {

    @Autowired
    private KafkaTemplate<String, String> stringKafkaTemplate;

    @Autowired
    private KafkaCustomProperties kafkaProperties;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @MockitoBean
    private AuditService auditService;

    private Consumer<String, String> dltConsumer;

    @BeforeEach
    void setUp() {
        processedEventRepository.deleteAll();

        doThrow(new RuntimeException("Audit failed"))
                .when(auditService)
                .recordEvent(any(UserRegisteredEvent.class));

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-transaction-dlt-" + UUID.randomUUID());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        dltConsumer = new KafkaConsumer<>(props);
        dltConsumer.subscribe(List.of(dltTopicName()));
    }

    @AfterEach
    void tearDown() {
        if (dltConsumer != null) {
            dltConsumer.close();
        }
    }

    @Test
    void should_not_mark_event_as_processed_when_business_logic_fails_and_send_to_dlt() throws Exception {
        UserRegisteredEvent event = UserRegisteredEvent.of(
                200L,
                "rollback-test@example.com"
        );

        ProcessedEventId processedEventId =
                new ProcessedEventId(event.eventId(), USER_EVENT_CONSUMER);

        String payload = objectMapper.writeValueAsString(event);

        sendInTransaction(
                userRegisteredTopicName(),
                String.valueOf(event.userId()),
                payload
        );

        await()
                .atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    assertThat(processedEventRepository.existsById(processedEventId))
                            .isFalse();

                    ConsumerRecords<String, String> records =
                            dltConsumer.poll(Duration.ofSeconds(2));

                    boolean found = StreamSupport.stream(records.spliterator(), false)
                            .anyMatch(record ->
                                    record.topic().equals(dltTopicName())
                                            && record.value().contains(event.eventId().toString())
                            );

                    assertThat(found).isTrue();
                });
    }

    private String userRegisteredTopicName() {
        return kafkaProperties.topics().userRegistered().name();
    }

    private String dltTopicName() {
        return userRegisteredTopicName() + ".DLT";
    }

    private void sendInTransaction(
            String topic,
            String key,
            String payload
    ) {
        stringKafkaTemplate.executeInTransaction(operations -> {
            try {
                operations.send(topic, key, payload)
                        .get(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return true;
        });
    }
}