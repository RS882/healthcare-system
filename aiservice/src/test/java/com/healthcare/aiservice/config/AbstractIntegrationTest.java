package com.healthcare.aiservice.config;

import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {
}
