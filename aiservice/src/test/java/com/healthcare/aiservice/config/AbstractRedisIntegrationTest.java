package com.healthcare.aiservice.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;


public abstract class AbstractRedisIntegrationTest extends AbstractIntegrationTest {

    private static final int REDIS_PORT = 6379;

    @Container
    protected static final GenericContainer<?> redis = new GenericContainer<>(
            DockerImageName.parse("redis:7.2-alpine")
    )
            .withExposedPorts(REDIS_PORT);


    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {

        registry.add(
                "spring.data.redis.host",
                redis::getHost
        );

        registry.add(
                "spring.data.redis.port",
                () -> redis.getMappedPort(REDIS_PORT)
        );
    }
}
