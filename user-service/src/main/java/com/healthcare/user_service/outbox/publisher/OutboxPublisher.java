package com.healthcare.user_service.outbox.publisher;

import com.healthcare.user_service.outbox.constant.OutboxStatus;
import com.healthcare.user_service.outbox.model.OutboxEvent;
import com.healthcare.user_service.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.healthcare.user_service.outbox.constant.PublisherConstant.MAX_ATTEMPTS;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxEventRepository repository;
    private final KafkaTemplate<String, String> stringKafkaTemplate;

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publish() {
        List<OutboxEvent> events = repository.findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.NEW);

        for (OutboxEvent event : events) {
            if (failIfMaxAttemptsExceeded(event)) {
                continue;
            }
            publishSingle(event);
        }
    }

    private boolean failIfMaxAttemptsExceeded(OutboxEvent event) {
        if (event.getAttemptCount() < MAX_ATTEMPTS) {
            return false;
        }

        event.setStatus(OutboxStatus.FAILED);
        event.setLastError("MAX_ATTEMPTS_EXCEEDED");
        return true;
    }

    private void publishSingle(OutboxEvent event) {
        event.setAttemptCount(event.getAttemptCount() + 1);

        try {
            stringKafkaTemplate.send(
                    event.getTopic(),
                    event.getAggregateId(),
                    event.getPayload()
            ).get(5, TimeUnit.SECONDS);

            event.setStatus(OutboxStatus.PUBLISHED);
            event.setPublishedAt(Instant.now());
            event.setLastError(null);

            log.info(
                    "Outbox event published: eventId={}, topic={}, attempts={}",
                    event.getEventId(),
                    event.getTopic(),
                    event.getAttemptCount()
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            handleFailure(event, e);
        } catch (ExecutionException e) {
            handleFailure(event, e.getCause() != null ? e.getCause() : e);
        } catch (Exception e) {
            handleFailure(event, e);
        }
    }

    private void handleFailure(OutboxEvent event, Throwable error) {
        String message = error.getMessage();
        if (message != null && message.length() > 2000) {
            message = message.substring(0, 2000);
        }

        event.setLastError(message);

        log.error(
                "Failed to publish outbox event: eventId={}, topic={}, attempts={}",
                event.getEventId(),
                event.getTopic(),
                event.getAttemptCount(),
                error
        );
    }
}