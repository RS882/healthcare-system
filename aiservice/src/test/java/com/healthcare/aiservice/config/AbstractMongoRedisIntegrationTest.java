package com.healthcare.aiservice.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

public abstract class AbstractMongoRedisIntegrationTest extends AbstractIntegrationTest {

    private static final int REDIS_PORT = 6379;

    @Container
    protected static final GenericContainer<?> redis = new GenericContainer<>(
            DockerImageName.parse("redis:7.2-alpine")
    )
            .withExposedPorts(REDIS_PORT);

    @Container
    protected static final MongoDBContainer mongo =
            new MongoDBContainer("mongo:7.0");


    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {

        registry.add(
                "spring.data.redis.host",
                redis::getHost
        );

        registry.add(
                "spring.data.redis.port",
                () -> redis.getMappedPort(REDIS_PORT)
        );

        registry.add(
                "spring.data.mongodb.uri",
                mongo::getReplicaSetUrl);
    }
}
