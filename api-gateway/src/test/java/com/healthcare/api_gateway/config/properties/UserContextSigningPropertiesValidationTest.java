package com.healthcare.api_gateway.config.properties;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class UserContextSigningPropertiesValidationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration.class,
                    org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration.class
            ))
            .withUserConfiguration(TestConfig.class);

    @Configuration
    @EnableConfigurationProperties(UserContextSigningProperties.class)
    static class TestConfig { }

    @Test
    void shouldBind_whenValid() {
        runner.withPropertyValues(
                "security.user-context.issuer=issuer-1",
                "security.user-context.key-id=kid-1",
                "security.user-context.default-ttl=PT10S"
        ).run(ctx -> {
            assertThat(ctx).hasNotFailed();

            UserContextSigningProperties props = ctx.getBean(UserContextSigningProperties.class);
            assertThat(props.issuer()).isEqualTo("issuer-1");
            assertThat(props.keyId()).isEqualTo("kid-1");
            assertThat(props.defaultTtl()).isEqualTo(Duration.ofSeconds(10));
        });
    }

    @Test
    void shouldFail_whenDefaultTtlTooSmall() {
        runner.withPropertyValues(
                "security.user-context.issuer=issuer-1",
                "security.user-context.key-id=kid-1",
                "security.user-context.default-ttl=PT0S"
        ).run(ctx -> {
            assertThat(ctx).hasFailed();
        });
    }

    @Test
    void shouldFail_whenIssuerBlank() {
        runner.withPropertyValues(
                "security.user-context.issuer=",
                "security.user-context.key-id=kid-1",
                "security.user-context.default-ttl=PT10S"
        ).run(ctx -> {
            assertThat(ctx).hasFailed();
        });
    }
}
