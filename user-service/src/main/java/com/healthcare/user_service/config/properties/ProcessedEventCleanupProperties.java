package com.healthcare.user_service.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "app.processed-event.cleanup")
public record ProcessedEventCleanupProperties(
        String cron,
        long retentionDays
) {

    public ProcessedEventCleanupProperties {
        if (!StringUtils.hasText(cron)) {
            cron = "0 30 3 * * *";
        }

        if (retentionDays <= 0) {
            retentionDays = 30;
        }
    }
}
