package com.healthcare.user_service.outbox.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "app.outbox.publisher.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class OutboxPublisher {

    private final OutboxPublishingService publishingService;

    @Scheduled(fixedDelay = 1000)
    public void publish() {
        List<Long> eventIds = publishingService.claimBatch();

        for (Long eventId : eventIds) {
            publishingService.publishSingle(eventId);
        }
    }
}