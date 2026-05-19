package com.healthcare.user_service.outbox.constant;

public enum OutboxStatus {
    NEW,
    PROCESSING,
    PUBLISHED,
    FAILED
}
