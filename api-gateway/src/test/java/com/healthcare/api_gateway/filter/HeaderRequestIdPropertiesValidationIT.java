package com.healthcare.api_gateway.filter;

import com.healthcare.api_gateway.config.HeaderRequestIdProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class HeaderRequestIdPropertiesValidationIT {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class))
            .withUserConfiguration(TestConfig.class);

    @EnableConfigurationProperties(HeaderRequestIdProperties.class)
    static class TestConfig {}

    @Test
    void shouldFailStartup_whenPropertyMissing() {
        contextRunner.run(context -> {
            assertThat(context).hasFailed();

            Throwable failure = context.getStartupFailure();
            Throwable root = failure;
            while (root.getCause() != null) {
                root = root.getCause();
            }

            assertThat(root.getMessage())
                    .contains("gateway.request-id.header")
                    .contains("name");
        });
    }

    @Test
    void shouldFailStartup_whenPropertyBlank() {
        contextRunner
                .withPropertyValues("gateway.request-id.header.name=   ")
                .run(context -> {
                    assertThat(context).hasFailed();

                    Throwable failure = context.getStartupFailure();
                    Throwable root = failure;
                    while (root.getCause() != null) {
                        root = root.getCause();
                    }

                    assertThat(root.getMessage())
                            .contains("gateway.request-id.header")
                            .contains("name");
                });
    }


    @Test
    void shouldStart_whenPropertyPresent() {
        contextRunner
                .withPropertyValues("gateway.request-id.header.name=X-Request-Id")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    HeaderRequestIdProperties props = context.getBean(HeaderRequestIdProperties.class);
                    assertThat(props.name()).isEqualTo("X-Request-Id");
                });
    }
}
