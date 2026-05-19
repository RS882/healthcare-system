package com.healthcare.user_service.outbox.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
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