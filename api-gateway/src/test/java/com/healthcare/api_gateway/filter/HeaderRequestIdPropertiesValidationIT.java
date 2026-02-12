package com.healthcare.api_gateway.filter;

import com.healthcare.api_gateway.config.properties.HeaderRequestIdProperties;
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

    private final String REQUEST_HEADER_ID_PROPS = "header-request-id.name";
    private final String REQUEST_HEADER_ID_VALUE = "X-Request-Id";
    private final String REQUEST_HEADER_ID_PROPS_NAME = "name";

    @EnableConfigurationProperties({HeaderRequestIdProperties.class})
    static class TestConfig {
    }

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
                    .contains(REQUEST_HEADER_ID_PROPS)
                    .contains(REQUEST_HEADER_ID_PROPS_NAME);
        });
    }

    @Test
    void shouldFailStartup_whenPropertyBlank() {
        contextRunner
                .withPropertyValues(REQUEST_HEADER_ID_PROPS + "." + REQUEST_HEADER_ID_PROPS_NAME + "=   ")
                .run(context -> {
                    assertThat(context).hasFailed();

                    Throwable failure = context.getStartupFailure();
                    Throwable root = failure;
                    while (root.getCause() != null) {
                        root = root.getCause();
                    }

                    assertThat(root.getMessage())
                            .contains(REQUEST_HEADER_ID_PROPS)
                            .contains(REQUEST_HEADER_ID_PROPS_NAME);
                });
    }


    @Test
    void shouldStart_whenPropertyPresent() {
        contextRunner
                .withPropertyValues(REQUEST_HEADER_ID_PROPS + "=" + REQUEST_HEADER_ID_VALUE)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    HeaderRequestIdProperties props = context.getBean(HeaderRequestIdProperties.class);
                    assertThat(props.name()).isEqualTo(REQUEST_HEADER_ID_VALUE);
                });
    }
}
