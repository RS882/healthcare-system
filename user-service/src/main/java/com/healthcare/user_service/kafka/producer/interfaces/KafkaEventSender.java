package com.healthcare.user_service.kafka.producer.interfaces;


import com.healthcare.user_service.outbox.model.OutboxEvent;

public interface KafkaEventSender {

    void send(OutboxEvent event);
}
