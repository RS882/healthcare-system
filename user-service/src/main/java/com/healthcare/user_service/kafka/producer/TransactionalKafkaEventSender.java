package com.healthcare.user_service.kafka.producer;


import com.healthcare.user_service.exception_handler.exception.KafkaEventSendException;
import com.healthcare.user_service.kafka.producer.interfaces.KafkaEventSender;
import com.healthcare.user_service.outbox.model.OutboxEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
public class TransactionalKafkaEventSender implements KafkaEventSender {

    private static final long SEND_TIMEOUT_SECONDS = 5;

    private final KafkaTemplate<String, String> stringKafkaTemplate;

    @Override
    public void send(OutboxEvent event) {
        stringKafkaTemplate.executeInTransaction(operations -> {
            sendWithinTransaction(event, operations);
            return true;
        });
    }

    private void sendWithinTransaction(
            OutboxEvent event,
            KafkaOperations<String, String> operations
    ) {
        try {
            operations.send(
                    event.getTopic(),
                    event.getAggregateId(),
                    event.getPayload()
            ).get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new KafkaEventSendException("Kafka send interrupted", e);
        } catch (ExecutionException e) {
            throw new KafkaEventSendException(
                    "Kafka send failed",
                    e.getCause() != null ? e.getCause() : e
            );
        } catch (TimeoutException e) {
            throw new KafkaEventSendException("Kafka send timed out", e);
        }
    }
}
