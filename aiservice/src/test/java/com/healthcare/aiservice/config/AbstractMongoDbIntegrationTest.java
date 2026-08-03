package com.healthcare.aiservice.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;


public abstract class AbstractMongoDbIntegrationTest extends AbstractIntegrationTest {

    @Container
    protected static final MongoDBContainer mongo =
            new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {

        registry.add(
                "spring.data.mongodb.uri",
                mongo::getReplicaSetUrl);

        registry.add(
                "spring.data.mongodb.auto-index-creation",
                () -> true
        );
    }
}
