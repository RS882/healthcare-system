package com.healthcare.user_service.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.processed-event.cleanup")
public record ProcessedEventCleanupProperties(
        String cron,
        long retentionDays
) {

    public ProcessedEventCleanupProperties {
        if (cron == null || cron.isBlank()) {
            cron = "0 30 3 * * *";
        }

        if (retentionDays <= 0) {
            retentionDays = 30;
        }
    }
}
