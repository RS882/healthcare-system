package com.healthcare.user_service.outbox.publisher;

import com.healthcare.user_service.exception_handler.exception.NotFoundException;
import com.healthcare.user_service.kafka.producer.interfaces.KafkaEventSender;
import com.healthcare.user_service.outbox.constant.OutboxStatus;
import com.healthcare.user_service.outbox.model.OutboxEvent;
import com.healthcare.user_service.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static com.healthcare.user_service.outbox.constant.OutboxConstant.MAX_ATTEMPTS;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxPublishingService {

    private static final int BATCH_SIZE = 100;

    private final OutboxEventRepository repository;
    private final KafkaEventSender kafkaEventSender;

    @Transactional
    public List<Long> claimBatch() {
        List<OutboxEvent> events = repository.findTopForUpdateByStatus(
                OutboxStatus.NEW,
                PageRequest.of(0, BATCH_SIZE)
        );

        events.forEach(event -> event.setStatus(OutboxStatus.PROCESSING));

        return events.stream()
                .map(OutboxEvent::getId)
                .toList();
    }

    @Transactional
    public void publishSingle(Long eventId) {
        OutboxEvent event = repository.findByIdAndStatus(
                        eventId,
                        OutboxStatus.PROCESSING
                )
                .orElseThrow(() -> new NotFoundException(
                        "Outbox event not found in PROCESSING status: id=" + eventId
                ));

        if (failIfMaxAttemptsExceeded(event)) {
            return;
        }

        event.setAttemptCount(event.getAttemptCount() + 1);

        try {
            kafkaEventSender.send(event);

            markPublished(event);

            log.info(
                    "Outbox event published: eventId={}, topic={}, attempts={}",
                    event.getEventId(),
                    event.getTopic(),
                    event.getAttemptCount()
            );
        } catch (Exception e) {
            markForRetry(event, e);
        }
    }

    private boolean failIfMaxAttemptsExceeded(OutboxEvent event) {
        if (event.getAttemptCount() < MAX_ATTEMPTS) {
            return false;
        }

        event.setStatus(OutboxStatus.FAILED);
        event.setLastError("MAX_ATTEMPTS_EXCEEDED");

        log.warn(
                "Outbox event failed permanently: eventId={}, attempts={}",
                event.getEventId(),
                event.getAttemptCount()
        );

        return true;
    }

    private void markPublished(OutboxEvent event) {
        event.setStatus(OutboxStatus.PUBLISHED);
        event.setPublishedAt(Instant.now());
        event.setLastError(null);
    }

    private void markForRetry(OutboxEvent event, Throwable error) {
        event.setStatus(OutboxStatus.NEW);
        event.setLastError(truncate(error.getMessage()));

        log.error(
                "Failed to publish outbox event: eventId={}, topic={}, attempts={}",
                event.getEventId(),
                event.getTopic(),
                event.getAttemptCount(),
                error
        );
    }

    private String truncate(String message) {
        if (message == null) {
            return null;
        }

        return message.length() > 2000
                ? message.substring(0, 2000)
                : message;
    }
}
