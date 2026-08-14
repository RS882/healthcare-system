package com.healthcare.user_service.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;

public class AbstractRedisMsqlTestContainer extends AbstractMySqlTestContainer{

    protected static final GenericContainer<?> redis =
            new GenericContainer<>("redis:7.4-alpine")
                    .withExposedPorts(6379);

    static {
        redis.start();
    }

    @DynamicPropertySource
    static void registerKafkaProperties(DynamicPropertyRegistry registry) {


        registry.add(
                "spring.data.redis.host",
                redis::getHost
        );

        registry.add(
                "spring.data.redis.port",
                () -> redis.getMappedPort(6379)
        );
    }
}
