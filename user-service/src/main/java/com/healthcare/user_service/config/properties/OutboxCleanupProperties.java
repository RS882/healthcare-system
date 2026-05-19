package com.healthcare.user_service.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.outbox.cleanup")
public record OutboxCleanupProperties(
        String cron,
        long retentionDays
) {

    public OutboxCleanupProperties {
        if (cron == null || cron.isBlank()) {
            cron = "0 0 3 * * *";
        }

        if (retentionDays <= 0) {
            retentionDays = 7;
        }
    }
}
