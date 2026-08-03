package com.healthcare.aiservice.config;

import com.healthcare.aiservice.config.propertie.PromptActivationRetryProperties;
import com.healthcare.aiservice.exception.AiPromptActivationRetryException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;

@Configuration
@EnableConfigurationProperties(
        PromptActivationRetryProperties.class
)
public class PromptActivationRetryConfiguration {

    public static final String PROMPT_ACTIVATION_RETRY_TEMPLATE =
            "promptActivationRetryTemplate";

    @Bean(PROMPT_ACTIVATION_RETRY_TEMPLATE)
    public RetryTemplate promptActivationRetryTemplate(
            PromptActivationRetryProperties properties
    ) {
        return RetryTemplate.builder()
                .maxAttempts(properties.maxAttempts())
                .exponentialBackoff(
                        properties.initialDelay(),
                        properties.multiplier(),
                        properties.maxDelay()
                )
                .retryOn(
                        AiPromptActivationRetryException.class
                )
                .build();
    }
}
