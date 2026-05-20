package com.healthcare.user_service.config.properties;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "app.outbox.recovery")
public record OutboxRecoveryProperties(
        String cron,
        long timeoutMinutes
) {

    public OutboxRecoveryProperties {
        if (!StringUtils.hasText(cron)) {
            cron = "0 */5 * * * *";
        }

        if (timeoutMinutes <= 0) {
            timeoutMinutes = 15;
        }
    }
}
