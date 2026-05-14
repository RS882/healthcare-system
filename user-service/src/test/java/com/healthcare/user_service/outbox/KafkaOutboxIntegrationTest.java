package com.healthcare.user_service.outbox;


import com.healthcare.user_service.audit.repository.AuditLogRepository;
import com.healthcare.user_service.kafka.idempotency.repository.ProcessedEventRepository;
import com.healthcare.user_service.outbox.constant.OutboxStatus;
import com.healthcare.user_service.outbox.model.OutboxEvent;
import com.healthcare.user_service.outbox.repository.OutboxEventRepository;

import com.healthcare.user_service.user.dto.RegistrationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class KafkaOutboxIntegrationTest {

    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:9.0.1")
            .withDatabaseName("healthcare_user_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static final KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.1")
    );

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);

        registry.add("app.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.liquibase.enabled", () -> "true");
    }

    @Autowired
    private UserService userService;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @Test
    void shouldPublishUserRegisteredEventAndProcessIt() {
        // given
        RegistrationRequest request = new RegistrationRequest(
                "test@example.com",
                "Password123!"
        );

        // when
        userService.registration(request);

        // then
        await()
                .atMost(15, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    List<OutboxEvent> outboxEvents = outboxEventRepository.findAll();

                    assertThat(outboxEvents).hasSize(1);

                    OutboxEvent outboxEvent = outboxEvents.get(0);

                    assertThat(outboxEvent.getStatus())
                            .isEqualTo(OutboxStatus.PUBLISHED);

                    assertThat(outboxEvent.getPublishedAt())
                            .isNotNull();

                    assertThat(outboxEvent.getAttemptCount())
                            .isGreaterThanOrEqualTo(1);

                    assertThat(outboxEvent.getLastError())
                            .isNull();

                    assertThat(auditLogRepository.findAll())
                            .hasSize(1);

                    assertThat(processedEventRepository.findAll())
                            .hasSize(1);
                });
    }
}
