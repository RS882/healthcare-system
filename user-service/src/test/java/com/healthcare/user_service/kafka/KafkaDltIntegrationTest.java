package com.healthcare.user_service.kafka;


import com.healthcare.user_service.config.AbstractKafkaMsqlTestContainer;
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

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("it")
@TestPropertySource(properties = {
        "user-context-filter.enabled=false"
})
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("Kafka DLT integration test")
class KafkaDltIntegrationTest extends AbstractKafkaMsqlTestContainer {

    @Autowired
    private KafkaTemplate<String, String> stringKafkaTemplate;

    @Autowired
    private KafkaCustomProperties kafkaProperties;

    private Consumer<String, String> dltConsumer;

    @BeforeEach
    void setUp() {
        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-dlt-consumer-" + UUID.randomUUID());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        dltConsumer = new KafkaConsumer<>(props);

        dltConsumer.subscribe(List.of(dltTopicName()));

        await()
                .atMost(Duration.ofSeconds(20))
                .until(() -> kafka.isRunning());
    }

    @AfterEach
    void tearDown() {
        if (dltConsumer != null) {
            dltConsumer.close();
        }
    }

    @Test
    void should_send_broken_user_registered_event_to_dlt_after_retries() throws Exception {
        String brokenPayload = """
                {
                  "broken": true
                }
                """;

        sendInTransaction(userRegisteredTopicName(),
                UUID.randomUUID().toString(),
                brokenPayload);

        await()
                .atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    ConsumerRecords<String, String> records =
                            dltConsumer.poll(Duration.ofSeconds(2));

                    boolean found = StreamSupport.stream(records.spliterator(), false)
                            .anyMatch(record ->
                                    record.topic().equals(dltTopicName())
                                            && record.value().contains("\"broken\": true")
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
                        .get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return true;
        });
    }
}